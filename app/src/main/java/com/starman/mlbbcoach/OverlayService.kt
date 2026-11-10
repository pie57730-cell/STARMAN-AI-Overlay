package com.starman.mlbbcoach

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    companion object {
        const val ACTION_UPDATE_ANALYSIS = "com.starman.mlbbcoach.ACTION_UPDATE_ANALYSIS"
        const val EXTRA_OCR_TEXT = "extra_ocr_text"
        var selectedUserRole: String = "GOLD_MM"
        var isLiveAnalysisRunning: Boolean = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val mainHandler = Handler(Looper.getMainLooper())
    private var mockCycleIndex = 0

    private val mockTickRunnable = object : Runnable {
        override fun run() {
            if (isLiveAnalysisRunning) {
                val sampleFrames = listOf(
                    "03:20 Early Game Yu Zhong Esmeralda Regen",
                    "08:15 Mid Game Moskov Inspire Attack Speed Ahead",
                    "11:40 Mid Game Vexana Burst Magic Turtle",
                    "15:10 Late Game Hylos Tank Behind Defend"
                )
                val frameText = sampleFrames[mockCycleIndex % sampleFrames.size]
                mockCycleIndex++
                updateUiWithReport(AIAnalysisEngine.analyzeScreenText(frameText, selectedUserRole))
                mainHandler.postDelayed(this, 4000L)
            }
        }
    }

    private val analysisReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_UPDATE_ANALYSIS && isLiveAnalysisRunning) {
                val ocrText = intent.getStringExtra(EXTRA_OCR_TEXT) ?: ""
                val report = AIAnalysisEngine.analyzeScreenText(ocrText, selectedUserRole)
                updateUiWithReport(report)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startOverlayForegroundNotification()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_starman, null)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 140
        }

        setupOverlayInteractions()
        windowManager.addView(overlayView, layoutParams)

        val filter = IntentFilter(ACTION_UPDATE_ANALYSIS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(analysisReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(analysisReceiver, filter)
        }
    }

    private fun setupOverlayInteractions() {
        val layoutExpanded = overlayView.findViewById<LinearLayout>(R.id.layoutExpanded)
        val layoutCompact = overlayView.findViewById<LinearLayout>(R.id.layoutCompact)
        val dragHeader = overlayView.findViewById<LinearLayout>(R.id.overlayHeaderBar)
        val btnMinimize = overlayView.findViewById<Button>(R.id.btnOverlayMinimize)
        val btnClose = overlayView.findViewById<Button>(R.id.btnOverlayClose)
        val btnExpand = overlayView.findViewById<Button>(R.id.btnOverlayExpand)
        val btnToggleAnalysis = overlayView.findViewById<Button>(R.id.btnToggleAnalysis)

        // Dragging support for both Expanded Header and Compact View
        val touchListener = object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayView, layoutParams)
                        return true
                    }
                }
                return false
            }
        }

        dragHeader.setOnTouchListener(touchListener)
        layoutCompact.setOnTouchListener(touchListener)

        btnMinimize.setOnClickListener {
            layoutExpanded.visibility = View.GONE
            layoutCompact.visibility = View.VISIBLE
        }

        btnExpand.setOnClickListener {
            layoutCompact.visibility = View.GONE
            layoutExpanded.visibility = View.VISIBLE
        }

        btnClose.setOnClickListener {
            stopSelf()
        }

        btnToggleAnalysis.setOnClickListener {
            isLiveAnalysisRunning = !isLiveAnalysisRunning
            if (isLiveAnalysisRunning) {
                btnToggleAnalysis.text = "[STOP LIVE ANALYSIS]"
                mainHandler.post(mockTickRunnable)
            } else {
                btnToggleAnalysis.text = "[START LIVE ANALYSIS]"
                mainHandler.removeCallbacks(mockTickRunnable)
            }
        }

        // 7 Tactical Command Buttons
        val commandMap = mapOf(
            R.id.btnCmdAttack to "ATTACK",
            R.id.btnCmdRotate to "ROTATE",
            R.id.btnCmdDefend to "DEFEND",
            R.id.btnCmdRetreat to "RETREAT",
            R.id.btnCmdObjective to "OBJECTIVE",
            R.id.btnCmdFarm to "FARM",
            R.id.btnCmdWait to "WAIT"
        )

        commandMap.forEach { (viewId, cmdName) ->
            overlayView.findViewById<Button>(viewId).setOnClickListener {
                val reminder = TacticalReminderEngine.getTacticalCommand(cmdName)
                applyTacticalCommandToUi(reminder)
            }
        }
    }

    private fun applyTacticalCommandToUi(reminder: TacticalReminder) {
        overlayView.findViewById<TextView>(R.id.tvTacticalSummary).text =
            "${reminder.command}: ${reminder.message}"
        overlayView.findViewById<TextView>(R.id.tvAiReasoning).text =
            "Sebab: ${reminder.reason} | Tindakan: ${reminder.nextAction}"
        overlayView.findViewById<TextView>(R.id.tvConfidence).text =
            "RISK: ${reminder.riskLevel}  |  CONFIDENCE: ${reminder.confidence}"
        overlayView.findViewById<TextView>(R.id.tvCompactStatus).text =
            "STARMAN [${reminder.command}]"
    }

    private fun updateUiWithReport(report: AnalysisReport) {
        overlayView.findViewById<TextView>(R.id.tvGamePhase).text = "Game Phase: ${report.gamePhase}"
        overlayView.findViewById<TextView>(R.id.tvTeamAdvantage).text = "Team Advantage: ${report.teamAdvantage}"
        overlayView.findViewById<TextView>(R.id.tvThreatLevel).text = "Threat Level: ${report.threatLevel}"

        overlayView.findViewById<TextView>(R.id.tvRecommendedItem).text =
            "REC [${report.itemAdvice.priority}]: ${report.itemAdvice.primaryItem.name} - ${report.itemAdvice.primaryReason}"
        overlayView.findViewById<TextView>(R.id.tvAlternativeItem).text =
            "ALT: ${report.itemAdvice.alternativeItem.name} - ${report.itemAdvice.alternativeReason}"

        applyTacticalCommandToUi(report.tacticalReminder)
    }

    private fun startOverlayForegroundNotification() {
        val channelId = "starman_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "STARMAN MLBB Floating Coach",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("STARMAN MLBB — Floating AI Coach")
            .setContentText("Overlay taktikal aktif di atas skrin.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(101, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isLiveAnalysisRunning = false
        mainHandler.removeCallbacks(mockTickRunnable)
        try {
            unregisterReceiver(analysisReceiver)
        } catch (_: Exception) {}
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
