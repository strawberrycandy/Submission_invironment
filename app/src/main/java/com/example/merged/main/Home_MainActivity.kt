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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.merged.R

class Home_MainActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val defaultTimerDurationMinutes = 5L

    // 現在のデータ保持用
    private var cherryBlossomGrowthStage: Int = 0
    private var tasksCompletedForGrowth: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // データの読み込み（ここでは表示のためだけに読み込む）
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        cherryBlossomGrowthStage = prefs.getInt("cherryBlossomGrowthStage", 0)
        tasksCompletedForGrowth = prefs.getInt("tasksWithThisCherryBlossom", 0)

        setupLayout(R.layout.activity_main)

        // 通知権限の確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        createNotificationChannel()
    }

    private fun setupLayout(layoutId: Int) {
        setContentView(layoutId)

        if (layoutId == R.layout.activity_main) {
            setupStartScreen()
        } else if (layoutId == R.layout.status_layout) {
            setupStatusScreen()
        }
        setupNavigationBar()
    }

    private fun setupStartScreen() {
        val startButton = findViewById<Button>(R.id.startButton)
        val timerDisplay = findViewById<TextView>(R.id.timer_display)
        val goNextButton = findViewById<Button>(R.id.go_break_task_button)
        val taskCountText = findViewById<TextView>(R.id.tasks_with_cherry_blossom_text)

        // タイマー表示初期化
        timerDisplay?.text = String.format(java.util.Locale.ROOT, "%02d:00", defaultTimerDurationMinutes)
        // タスク回数表示
        taskCountText?.text = "この桜とのタスク回数: ${tasksCompletedForGrowth}回"

        // 画像を現在のステージに合わせる
        updateTreeImageByStage(cherryBlossomGrowthStage)

        startButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

        startButton?.setOnClickListener {
            startTimer(defaultTimerDurationMinutes)
            setupLayout(R.layout.status_layout)
        }
    }

    private fun setupStatusScreen() {
        updateTreeImageByStage(cherryBlossomGrowthStage)
        findViewById<Button>(R.id.stopButton)?.setOnClickListener {
            stopTimer()
            setupLayout(R.layout.activity_main)
        }
    }

    private fun startTimer(durationMinutes: Long) {
        val durationMillis = durationMinutes * 1 * 1000
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000
                findViewById<TextView>(R.id.timer_display)?.text =
                    String.format(java.util.Locale.ROOT, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)
            }

            override fun onFinish() {
                isTimerRunning = false
                // 🚨 ここでは成長させない！ 🚨
                // UIのみ更新して、休憩画面への誘導ボタンを出す
                findViewById<TextView>(R.id.timer_display)?.text = "00:00"
                findViewById<Button>(R.id.stopButton)?.visibility = View.GONE

                val goNextButton = findViewById<Button>(R.id.go_break_task_button)
                goNextButton?.visibility = View.VISIBLE
                goNextButton?.setOnClickListener {
                    // ここで初めて休憩タスク画面へ遷移
                    startActivity(Intent(this@Home_MainActivity, Task_MainActivity::class.java))
                }
                Toast.makeText(this@Home_MainActivity, "タイマー終了！休憩へ進んでください", Toast.LENGTH_SHORT).show()
            }
        }.start()
        isTimerRunning = true
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun updateTreeImageByStage(stage: Int) {
        val imageView = findViewById<ImageView>(R.id.sakura_image) ?: findViewById<ImageView>(R.id.tree_image)
        val resId = when (stage) {
            1 -> R.drawable.sakura_stage_1
            2 -> R.drawable.sakura_stage_2
            3 -> R.drawable.sakura_stage_3
            4 -> R.drawable.sakura_stage_4
            else -> R.drawable.sakura_stage_0
        }
        imageView?.setImageResource(resId)
    }

    private fun setupNavigationBar() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener { setupLayout(R.layout.activity_main) }
        findViewById<View>(R.id.nav_status)?.setOnClickListener { startActivity(Intent(this, StatusActivity::class.java)) }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<View>(R.id.nav_result)?.setOnClickListener { startActivity(Intent(this, TaskStatsActivity::class.java)) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("eye_rest_channel", "休憩通知", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}