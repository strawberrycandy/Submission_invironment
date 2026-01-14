package com.example.merged.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import com.example.merged.R
import com.example.merged.first_setup.Test
import android.app.PendingIntent

class Home_MainActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val defaultTimerDurationMinutes = 1L
    private var currentLayoutId: Int = R.layout.activity_main

    private var cherryBlossomGrowthStage: Int = 0
    private var tasksCompletedForGrowth: Int = 0

    private val TASKS_PER_GROWTH_STAGE = 2
    private val CHERRY_BLOSSOM_GROWTH_STAGE_MAX = 4
    private val CHERRY_BLOSSOM_GROWTH_STAGE_MIN = 0
    private val CHERRY_BLOSSOM_STATUS_MAX = 2
    private val CHERRY_BLOSSOM_STATUS_MIN = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                println("通知許可が与えられました")
            } else {
                println("通知許可が拒否されました")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        cherryBlossomGrowthStage = prefs.getInt("cherryBlossomGrowthStage", 0)
        tasksCompletedForGrowth = prefs.getInt("tasksWithThisCherryBlossom", 0)

        super.onCreate(savedInstanceState)
        updateCherryBlossomStage()
        setupLayout(R.layout.activity_main)

        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        nav?.itemIconTintList = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        createNotificationChannel()
    }

    private fun setupLayout(layoutId: Int) {
        setContentView(layoutId)
        currentLayoutId = layoutId

        if (layoutId == R.layout.activity_main) {
            setupStartScreen()
        } else if (layoutId == R.layout.status_layout) {
            setupStatusScreen()
        }

        setupNavigationBar()
        setNavigationSelection()
    }

    private fun setupStartScreen() {
        val startButton = findViewById<Button>(R.id.startButton)
        val timerDisplay = findViewById<TextView>(R.id.timer_display)
        val goNextButton = findViewById<Button>(R.id.go_break_task_button)

        if (isTimerRunning) {
            setupLayout(R.layout.status_layout)
            return
        }

        timerDisplay?.text = String.format(java.util.Locale.ROOT, "%02d:00", defaultTimerDurationMinutes)
        startButton?.text = getString(R.string.button_start)
        startButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

        updateCherryBlossomStage()

        startButton?.setOnClickListener {
            startTimer(defaultTimerDurationMinutes)
            Toast.makeText(this, "タイマーを開始しました", Toast.LENGTH_SHORT).show()
            scheduleNotification()
            setupLayout(R.layout.status_layout)
        }
    }

    private fun setupStatusScreen() {
        val stopButton = findViewById<Button>(R.id.stopButton)
        val goNextButton = findViewById<Button>(R.id.go_break_task_button)

        if (!isTimerRunning && goNextButton?.visibility != View.VISIBLE) {
            setupLayout(R.layout.activity_main)
            return
        }

        updateTreeImageByStage(cherryBlossomGrowthStage)

        if (isTimerRunning) {
            stopButton?.visibility = View.VISIBLE
            goNextButton?.visibility = View.GONE
        }

        stopButton?.setOnClickListener {
            stopTimer()
            Toast.makeText(this, "タイマーを停止しました", Toast.LENGTH_SHORT).show()
            setupLayout(R.layout.activity_main)
        }
    }

    private fun startTimer(durationMinutes: Long) {
        val durationMillis = durationMinutes * 60 * 1000 // ミリ秒修正
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val formattedTime = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)
                findViewById<TextView>(R.id.timer_display)?.text = formattedTime
            }

            override fun onFinish() {
                isTimerRunning = false
                tasksCompletedForGrowth++
                updateCherryBlossomStage()

                findViewById<TextView>(R.id.timer_display)?.text = "00:00"
                findViewById<Button>(R.id.stopButton)?.visibility = View.GONE

                val goNextButton = findViewById<Button>(R.id.go_break_task_button)
                goNextButton?.visibility = View.VISIBLE

                goNextButton?.setOnClickListener {
                    val intent = Intent(this@Home_MainActivity, Task_MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                Toast.makeText(this@Home_MainActivity, "休憩時間です！", Toast.LENGTH_LONG).show()
            }
        }.start()
        isTimerRunning = true
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        WorkManager.getInstance(this).cancelAllWork()
    }

    private fun updateCherryBlossomStage() {
        val newStage = tasksCompletedForGrowth / TASKS_PER_GROWTH_STAGE
        val limitedStage = newStage.coerceIn(CHERRY_BLOSSOM_GROWTH_STAGE_MIN, CHERRY_BLOSSOM_GROWTH_STAGE_MAX)
        cherryBlossomGrowthStage = limitedStage

        val taskCountText = findViewById<TextView>(R.id.tasks_with_cherry_blossom_text)
        taskCountText?.text = "この桜とのタスク回数: ${tasksCompletedForGrowth}回"
        updateTreeImageByStage(cherryBlossomGrowthStage)
    }

    private fun updateTreeImageByStage(stage: Int) {
        val imageView = findViewById<ImageView>(R.id.sakura_image) ?: findViewById<ImageView>(R.id.tree_image)
        if (imageView == null) return

        val drawableResId = when (stage) {
            0 -> R.drawable.sakura_stage_0
            1 -> R.drawable.sakura_stage_1
            2 -> R.drawable.sakura_stage_2
            3 -> R.drawable.sakura_stage_3
            else -> R.drawable.sakura_stage_0
        }
        imageView.setImageResource(drawableResId)
    }

    // ★★★ 修正箇所: ナビゲーションバーのロックロジック ★★★
    private fun setupNavigationBar() {
        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        nav?.itemIconTintList = null

        // ヘルパー関数: タイマー終了後のボタンが出ているかチェック
        fun isFinishingState(): Boolean {
            val goNextButton = findViewById<Button>(R.id.go_break_task_button)
            return !isTimerRunning && goNextButton?.visibility == View.VISIBLE
        }

        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            if (isFinishingState()) return@setOnClickListener // 終了時は無視
            if (isTimerRunning) {
                setupLayout(R.layout.status_layout)
            } else {
                setupLayout(R.layout.activity_main)
            }
        }

        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            if (isFinishingState()) return@setOnClickListener // 終了時は無視
            val intent = Intent(this, StatusActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            if (isFinishingState()) return@setOnClickListener // 終了時は無視
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.nav_result)?.setOnClickListener {
            if (isFinishingState()) return@setOnClickListener // 終了時は無視
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setNavigationSelection() {
        val selectedNavId = when (currentLayoutId) {
            R.layout.activity_main, R.layout.status_layout -> R.id.nav_home
            else -> R.id.nav_home
        }
        resetNavigationColors()
        val navItemView = findViewById<View>(selectedNavId)
        val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
        val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)
        val activeColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        navIcon?.setColorFilter(activeColor)
        navLabel?.setTextColor(activeColor)
    }

    private fun resetNavigationColors() {
        val navItems = listOf(R.id.nav_home, R.id.nav_status, R.id.nav_settings, R.id.nav_result)
        val defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId)
            val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)
            navIcon?.setColorFilter(defaultColor)
            navLabel?.setTextColor(defaultColor)
        }
    }

    private fun scheduleNotification() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(defaultTimerDurationMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "eye_rest_channel",
                "休憩通知チャンネル",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val intent = Intent(context, Task_MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, "eye_rest_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("休憩の時間です")
            .setContentText("目を休ませましょう")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }
        return Result.success()
    }
}