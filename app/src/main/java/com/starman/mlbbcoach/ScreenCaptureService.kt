package com.starman.mlbbcoach

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScreenCaptureService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val captureHandler = Handler(Looper.getMainLooper())
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastFrameHash: Int = 0

    private val periodicCaptureRunnable = object : Runnable {
        override fun run() {
            if (OverlayService.isLiveAnalysisRunning) {
                processLatestScreenFrame()
            }
            // Analisis berkala setiap 3.5 saat untuk menjimatkan bateri & CPU
            captureHandler.postDelayed(this, 3500L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCaptureForegroundNotification()

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (resultCode == Activity.RESULT_OK && resultData != null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
            setupVirtualDisplay()
            captureHandler.postDelayed(periodicCaptureRunnable, 2000L)
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun setupVirtualDisplay() {
        val metrics = resources.displayMetrics
        val width = 720
        val height = 1280
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "StarmanMLBBCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            captureHandler
        )
    }

    private fun processLatestScreenFrame() {
        val reader = imageReader ?: return
        val image = reader.acquireLatestImage() ?: return

        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Semak perubahan frame mudah untuk elak proses berulang jika skrin statik
            val samplePixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 4)
            if (samplePixel == lastFrameHash) {
                bitmap.recycle()
                return
            }
            lastFrameHash = samplePixel

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val detectedText = visionText.text
                    val updateIntent = Intent(OverlayService.ACTION_UPDATE_ANALYSIS).apply {
                        setPackage(packageName)
                        putExtra(OverlayService.EXTRA_OCR_TEXT, detectedText)
                    }
                    sendBroadcast(updateIntent)
                    bitmap.recycle()
                }
                .addOnFailureListener {
                    bitmap.recycle()
                }
        } catch (_: Exception) {
        } finally {
            image.close()
        }
    }

    private fun startCaptureForegroundNotification() {
        val channelId = "starman_capture_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "STARMAN Live Screen Analysis",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("STARMAN Live Screen Analysis")
            .setContentText("Menganalisis maklumat skrin MLBB dengan kebenaran pengguna.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

        startForeground(102, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        captureHandler.removeCallbacks(periodicCaptureRunnable)
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}w
