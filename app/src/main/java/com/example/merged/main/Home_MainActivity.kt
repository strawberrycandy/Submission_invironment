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


class Home_MainActivity : AppCompatActivity() {


    // ★★★ 桜の成長に関する定数と変数 (新規/修正) ★★★
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val defaultTimerDurationMinutes = 30L
    private var currentLayoutId: Int = R.layout.activity_main

    // 桜の成長段階 (0〜4)
    private var cherryBlossomGrowthStage: Int = 0
    // タスク達成回数 (この回数に応じて成長段階が決定される)
    private var tasksCompletedForGrowth: Int = 0

    // 成長の基準となる定数
    private val TASKS_PER_GROWTH_STAGE = 2 // 1段階成長するのに必要なタスク回数 (例として2に設定)
    private val CHERRY_BLOSSOM_GROWTH_STAGE_MAX = 4
    private val CHERRY_BLOSSOM_GROWTH_STAGE_MIN = 0
    private val CHERRY_BLOSSOM_STATUS_MAX = 2 // 使用しないが定義
    private val CHERRY_BLOSSOM_STATUS_MIN = 0 // 使用しないが定義

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

        // ★★★ 変更: アプリ起動時に成長状態をチェックし、UIに反映させる ★★★
        updateCherryBlossomStage()

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

        timerDisplay?.text = String.format(java.util.Locale.ROOT, "%02d:00", defaultTimerDurationMinutes)

        startButton?.text = getString(R.string.button_start)
        startButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

        // ★★★ 修正: 画面表示時に成長段階とタスク回数をUIに反映 ★★★
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

        if (!isTimerRunning) {
            setupLayout(R.layout.activity_main)
            return
        }

        // ★★★ 追加: タイマー実行中の画面でも固定された成長段階の画像をセットする ★★★
        updateTreeImageByStage(cherryBlossomGrowthStage)

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
                val remainingSeconds = millisUntilFinished / 1000
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val formattedTime = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)

                findViewById<TextView>(R.id.timer_display)?.text = formattedTime

                // ★★★ 削除: タイマー同時進行の画像更新処理を削除しました ★★★
            }

            override fun onFinish() {
                isTimerRunning = false

                // ★★★ 修正: タスク回数を増やし、桜を成長させる ★★★
                tasksCompletedForGrowth++
                updateCherryBlossomStage()

                findViewById<TextView>(R.id.timer_display)?.text = "00:00"
                findViewById<Button>(R.id.stopButton)?.visibility = View.GONE

                val goNextButton = findViewById<Button>(R.id.go_break_task_button)
                goNextButton?.visibility = View.VISIBLE

                goNextButton?.setOnClickListener {
                    val intent = Intent(this@Home_MainActivity, Task_MainActivity::class.java)
                    startActivity(intent)
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

    // --- 桜の成長ロジック (新規追加) ---

    /**
     * タスク達成回数に基づいて桜の成長段階を計算し、UIを更新する
     */
    private fun updateCherryBlossomStage() {
        // 成長段階を計算 (例: 2タスクで1段階アップ)
        val newStage = tasksCompletedForGrowth / TASKS_PER_GROWTH_STAGE

        // 成長段階を最大値(4)までに制限
        val limitedStage = newStage.coerceIn(CHERRY_BLOSSOM_GROWTH_STAGE_MIN, CHERRY_BLOSSOM_GROWTH_STAGE_MAX)

        cherryBlossomGrowthStage = limitedStage

        // タスク達成回数表示の更新
        val taskCountText = findViewById<TextView>(R.id.tasks_with_cherry_blossom_text)
        taskCountText?.text = "この桜とのタスク回数: ${tasksCompletedForGrowth}回"

        // 桜画像の更新
        updateTreeImageByStage(cherryBlossomGrowthStage)
    }

    /**
     * 成長段階に基づいて桜の画像リソースを切り替える
     */
    private fun updateTreeImageByStage(stage: Int) {
        // activity_main.xml (startButtonの時) と status_layout_running.xml (stopButtonの時) の両方に対応
        val imageView = findViewById<ImageView>(R.id.sakura_image) ?: findViewById<ImageView>(R.id.tree_image)
        if (imageView == null) return

        val drawableResId = when (stage) {
            0 -> R.drawable.sakura_stage_0
            1 -> R.drawable.sakura_stage_1
            2 -> R.drawable.sakura_stage_2 // 必要なリソースIDに置き換えてください
            3 -> R.drawable.sakura_stage_3
            //4 -> R.drawable.sakura_stage_4 // 必要なリソースIDに置き換えてください
            else -> R.drawable.sakura_stage_0
        }
        imageView.setImageResource(drawableResId)
    }

    // --- ナビゲーション処理 (変更なし) ---
    // ... (setupNavigationBar, setNavigationSelection, resetNavigationColors は省略) ...

    private fun setupNavigationBar() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            if (isTimerRunning) {
                setupLayout(R.layout.status_layout)
            } else {
                setupLayout(R.layout.activity_main)
            }
        }

        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            val intent = Intent(this, StatusActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.nav_result)?.setOnClickListener {
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
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_status,
            R.id.nav_settings,
            R.id.nav_result
        )

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

// --- NotificationWorker (変更なし) ---
class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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