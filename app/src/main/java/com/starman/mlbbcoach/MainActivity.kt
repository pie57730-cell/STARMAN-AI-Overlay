package com.starman.mlbbcoach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var spinnerRole: Spinner
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedRole = spinnerRole.selectedItem?.toString() ?: "GOLD_MM"
            OverlayService.selectedUserRole = selectedRole

            // Mulakan Floating Overlay Service
            val overlayIntent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }

            // Mulakan ScreenCaptureService dengan token rasmi MediaProjection
            val captureIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(captureIntent)
            } else {
                startService(captureIntent)
            }

            Toast.makeText(this, "STARMAN MLBB Aktif! Anda boleh membuka MLBB sekarang.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Kebenaran Screen Capture diperlukan untuk analisis masa nyata.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
        spinnerRole = findViewById(R.id.spinnerUserRole)

        val roles = listOf("GOLD_MM", "MID_MAGE", "EXP_FIGHTER", "ROAM_TANK", "JUNGLE_ASSASSIN")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        findViewById<Button>(R.id.btnGrantOverlay).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.btnStartOverlayOnly).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            OverlayService.selectedUserRole = spinnerRole.selectedItem?.toString() ?: "GOLD_MM"
            val overlayIntent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent)
            } else {
                startService(overlayIntent)
            }
            Toast.makeText(this, "Floating Overlay STARMAN dilancarkan!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStartCaptureAndOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }

        findViewById<Button>(R.id.btnStopAllServices).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            stopService(Intent(this, ScreenCaptureService::class.java))
            Toast.makeText(this, "Semua servis STARMAN telah dihentikan.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val hasOverlay = Settings.canDrawOverlays(this)
        tvPermissionStatus.text = if (hasOverlay) {
            "STATUS OVERLAY: DIBENARKAN (SEDIA DIGUNAKAN)"
        } else {
            "STATUS OVERLAY: BELUM DIBENARKAN (TEKAN BUTANG 1)"
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, "Kebenaran Overlay sudah aktif!", Toast.LENGTH_SHORT).show()
        }
    }
}w
