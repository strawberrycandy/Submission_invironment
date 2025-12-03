package com.example.myfirstapp

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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    // タイマー関連のメンバー変数
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val defaultTimerDurationMinutes = 1L // タイマー時間を30分に戻しました
    private var currentLayoutId: Int = R.layout.activity_main

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                println("通知許可が与えられました")
            } else {
                println("通知許可が拒否されました")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLayout(R.layout.activity_main)

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



    // --- 画面ごとのロジック ---

    private fun setupStartScreen() {
        val startButton = findViewById<Button>(R.id.startButton)
        val timerDisplay = findViewById<TextView>(R.id.timer_display)
        val goNextButton = findViewById<Button>(R.id.go_break_task_button)

        if (isTimerRunning) {
            setupLayout(R.layout.status_layout)
            return
        }

        // デフォルトロケールに関する警告を無視するため、Locale.ROOT を追加 (必須ではないが推奨)
        timerDisplay?.text = String.format(java.util.Locale.ROOT, "%02d:00", defaultTimerDurationMinutes)

        startButton?.text = getString(R.string.button_start)
        startButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

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

        if (!isTimerRunning) {
            setupLayout(R.layout.activity_main)
            return
        }

        stopButton?.text = getString(R.string.button_stop)
        stopButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

        stopButton?.setOnClickListener {
            stopTimer()
            Toast.makeText(this, "タイマーを停止しました", Toast.LENGTH_SHORT).show()
            setupLayout(R.layout.activity_main)
        }
    }

    // --- タイマー処理 ---

    private fun startTimer(durationMinutes: Long) {
        val durationMillis = durationMinutes * 60 * 1000
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = durationMinutes * 60
                val remainingSeconds = millisUntilFinished / 1000
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                // デフォルトロケールに関する警告を無視するため、Locale.ROOT を追加
                val formattedTime = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)

                findViewById<TextView>(R.id.timer_display)?.text = formattedTime

                updateTreeImage(totalSeconds - remainingSeconds)
            }

            override fun onFinish() {
                isTimerRunning = false

                findViewById<TextView>(R.id.timer_display)?.text = "00:00"
                findViewById<Button>(R.id.stopButton)?.visibility = View.GONE

                // 体操画面へ遷移するボタンを表示
                val goNextButton = findViewById<Button>(R.id.go_break_task_button)
                goNextButton?.visibility = View.VISIBLE

                goNextButton?.setOnClickListener {
                    val intent = Intent(this@MainActivity, BreakTaskActivity::class.java)
                    startActivity(intent)
                }

                Toast.makeText(this@MainActivity, "休憩時間です！", Toast.LENGTH_LONG).show()
            }
        }.start()

        isTimerRunning = true
    }

    private fun updateTreeImage(elapsedSeconds: Long) {
        val totalSeconds = defaultTimerDurationMinutes * 60
        val progress = elapsedSeconds.toFloat() / totalSeconds
        val imageView = findViewById<ImageView>(R.id.tree_image)

        if (imageView == null) return

        // 進行度に応じて画像を変更 (tree_stage_X が res/drawable/ に存在することが前提)
        val drawableResId = when {
            progress < 0.25f -> R.drawable.sakura_stage_0
            progress < 0.50f -> R.drawable.sakura_stage_1
            progress < 0.75f -> R.drawable.sakura_stage_3
            else -> R.drawable.tree_stage_4
        }
        imageView.setImageResource(drawableResId)
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        WorkManager.getInstance(this).cancelAllWork()
    }

    // --- ナビゲーション処理 ---

    private fun setupNavigationBar() {
        // HOMEボタン (タイマー画面の切り替え)
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            if (isTimerRunning) {
                setupLayout(R.layout.status_layout)
            } else {
                setupLayout(R.layout.activity_main)
            }
        }

        // STATUSボタン (新しいStatusActivityへ画面遷移)
        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            val intent = Intent(this, StatusActivity::class.java)
            startActivity(intent)
        }
        // 他のナビゲーション項目 (FAVORITE, SETTINGS, RESULT) のロジックは省略
    }

    private fun setNavigationSelection() {
        // 現在のレイアウトIDに基づいて、ハイライトするナビゲーションボタンを決定
        val selectedNavId = when (currentLayoutId) {
            R.layout.activity_main, R.layout.status_layout -> R.id.nav_home // HOMEボタンをハイライト
            // 他のレイアウトIDがあれば、ここで nav_favorite, nav_settings, nav_result を選択
            else -> R.id.nav_home // デフォルト
        }

        // すべてのナビゲーションアイコンの色をリセット
        resetNavigationColors()

        // 選択されたボタンをハイライト
        val navItemView = findViewById<View>(selectedNavId)

        val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
        val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)

        // 選択状態の色設定 (一時的にAndroidのデフォルトの緑色を使用)
        val activeColor = resources.getColor(android.R.color.holo_green_dark, theme)

        navIcon?.setColorFilter(activeColor)
        navLabel?.setTextColor(activeColor)
    }

    private fun resetNavigationColors() {
        // XML (layout_navigation_bar.xml) に定義されているIDに一致させる
        val navItems = listOf(
            R.id.nav_home, // HOMEボタンを追加
            R.id.nav_status,
            R.id.nav_favorite,
            R.id.nav_settings,
            R.id.nav_result
        )

        // リセットする色 (例: 灰色)
        val defaultColor = resources.getColor(android.R.color.darker_gray, theme)

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

// --- NotificationWorker ---
class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(context, "eye_rest_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("休憩の時間です")
            .setContentText("目を休ませましょう")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }

        return Result.success()
    }
}
