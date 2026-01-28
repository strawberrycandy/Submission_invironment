package com.example.merged.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
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
import android.app.PendingIntent
import android.util.Log
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.os.Handler
import android.os.Looper
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.random.Random
import com.example.merged.util.BugManager




class Home_MainActivity : AppCompatActivity() {


    // ★★★ 桜の成長に関する定数と変数 (新規/修正) ★★★
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false // タイマーが動いているかのフラグ
    private val defaultTimerDurationMinutes = 1L // ここでタイマーの時間を調整できます(1L = 1分, 30L = 30分)
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

    // 小鳥を動かすための変数
    private val birdHandler = Handler(Looper.getMainLooper())
    private var birdRunnable: Runnable? = null
    private val birdResList = listOf(R.drawable.suzume, R.drawable.mejiro)

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
        prefs.edit().putInt("TASKS_PER_GROWTH_STAGE", TASKS_PER_GROWTH_STAGE).apply()

        super.onCreate(savedInstanceState)

        setupLayout(R.layout.activity_main)

        // ★★★ 変更: アプリ起動時に成長状態をチェックし、UIに反映させる ★★★
        updateCherryBlossomStage()

        setupLayout(R.layout.activity_main)

        // ナビゲーションバーを取得して、色の自動変更を無効にする
        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        nav?.itemIconTintList = null
        nav?.itemTextColor = null // 文字の自動変色もオフにする

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
        // SoundManagerを初期化
        SoundManager.init(this)
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
            SoundManager.playSE(this)
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
            startBirdLoop()
            return
        }

        // 1. 画像の更新処理
        // status_layout上のImageView(tree_image)を確実に取得して更新します
        updateTreeImageByStage(cherryBlossomGrowthStage)


        // stopButton?.text = getString(R.string.button_stop)

        stopButton?.visibility = View.VISIBLE
        goNextButton?.visibility = View.GONE

        stopButton?.setOnClickListener {
            SoundManager.playSE(this)
            stopTimer()
            Toast.makeText(this, "タイマーを停止しました", Toast.LENGTH_SHORT).show()
            setupLayout(R.layout.activity_main)
        }
    }

    // --- タイマー処理 ---

    private fun startTimer(durationMinutes: Long) {
        val durationMillis = durationMinutes * 60 * 1000 / 6

        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                // ここに書かないとタイマーカウントダウン中にタスク回数の表示が出ない;;
                val taskCountText = findViewById<TextView>(R.id.tasks_with_cherry_blossom_text)
                taskCountText?.text = "この桜とのタスク回数: ${tasksCompletedForGrowth}回"

                val remainingSeconds = millisUntilFinished / 1000
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val formattedTime = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)

                findViewById<TextView>(R.id.timer_display)?.text = formattedTime

                // ★★★ 削除: タイマー同時進行の画像更新処理を削除しました ★★★
            }

            override fun onFinish() {
                isTimerRunning = false

                // グラフ統計用のデータを保存する
                com.example.merged.util.TaskStatsManager.saveTaskCompleted(this@Home_MainActivity)

                updateCherryBlossomStage()

                findViewById<TextView>(R.id.timer_display)?.text = "00:00"
                findViewById<Button>(R.id.stopButton)?.visibility = View.GONE

                val goNextButton = findViewById<Button>(R.id.go_break_task_button)
                goNextButton?.visibility = View.VISIBLE

                goNextButton?.setOnClickListener {
                    SoundManager.playSE(this@Home_MainActivity)
                    val intent = Intent(this@Home_MainActivity, Task_MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                Toast.makeText(this@Home_MainActivity, "休憩時間です！", Toast.LENGTH_LONG).show()
            }
        }.start()

        isTimerRunning = true

        // 小鳥たちが動き始めます
        startBirdLoop()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false

        // 小鳥たちが、止まります、、、、、、、、、
        stopBirdLoop()
        WorkManager.getInstance(this).cancelAllWork()
    }

    // --- 桜の成長ロジック (新規追加) ---

    // タスク達成回数に基づいて桜の成長段階を計算し、UIを更新する
    private fun updateCherryBlossomStage() {
        // 成長段階を計算 (例: 2タスクで1段階アップ)
        val newStage = tasksCompletedForGrowth / TASKS_PER_GROWTH_STAGE

        // 成長段階を最大値(4)までに制限
        val limitedStage = newStage.coerceIn(CHERRY_BLOSSOM_GROWTH_STAGE_MIN, CHERRY_BLOSSOM_GROWTH_STAGE_MAX)

        cherryBlossomGrowthStage = limitedStage

        // タスク達成回数表示の更新
        val taskCountText = findViewById<TextView>(R.id.tasks_with_cherry_blossom_text)
        taskCountText?.text = "この桜とのタスク回数: ${tasksCompletedForGrowth}回"

        Log.d("Home_MainActivity", "${tasksCompletedForGrowth}")
        // 桜画像の更新
        updateTreeImageByStage(cherryBlossomGrowthStage)

        // 虫カウンターの更新
        updateBugDisplay()
    }

    /**
     * 虫の数を取得してUIに反映させる
     */
    private fun updateBugDisplay() {
        if (currentLayoutId != R.layout.activity_main) {
            Log.d("BugDisplay", "Not in activity_main.xml, skipping bug display update.")
            return
        }
        val bugCount = BugManager.getBugCount(this)
        Log.d("BugDisplay", "Current Bug Count: $bugCount")

        if (bugCount >= 3) {
            Log.d("BugDisplay", "Bug count reached 3 or more. Applying penalty.")
            if (cherryBlossomGrowthStage > CHERRY_BLOSSOM_GROWTH_STAGE_MIN) {
                cherryBlossomGrowthStage-- // Decrease stage
                // Update tasksCompletedForGrowth to reflect new stage
                tasksCompletedForGrowth = cherryBlossomGrowthStage * TASKS_PER_GROWTH_STAGE
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().putInt("cherryBlossomGrowthStage", cherryBlossomGrowthStage).apply()
                prefs.edit().putInt("tasksWithThisCherryBlossom", tasksCompletedForGrowth).apply()
                BugManager.resetBugs(this) // Reset bugs after applying penalty
                updateCherryBlossomStage() // Update UI to reflect new stage
                Log.d("BugDisplay", "Penalty applied: stage decreased to $cherryBlossomGrowthStage, bugs reset.")
                // No need to update bug display again here, as updateCherryBlossomStage() will trigger it.
                return // Exit early as stage change will re-trigger updateBugDisplay
            } else {
                // Cannot decrease stage further, just reset bugs to prevent infinite penalty
                BugManager.resetBugs(this)
                Log.d("BugDisplay", "Cannot decrease stage further, bugs reset.")
            }
        }

        val bugImageViews = listOf(
            findViewById<ImageView>(R.id.bug_image_1),
            findViewById<ImageView>(R.id.bug_image_2),
            findViewById<ImageView>(R.id.bug_image_3),
            findViewById<ImageView>(R.id.bug_image_4),
            findViewById<ImageView>(R.id.bug_image_5)
        )

        bugImageViews.forEachIndexed { index, imageView ->
            if (imageView == null) {
                Log.e("BugDisplay", "Bug image ${index + 1} (R.id.bug_image_${index + 1}) not found in layout!")
            }
            imageView?.visibility = if (index < bugCount) {
                Log.d("BugDisplay", "Bug image ${index + 1} set to VISIBLE")
                View.VISIBLE
            } else {
                Log.d("BugDisplay", "Bug image ${index + 1} set to GONE")
                View.GONE
            }
        }
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
            2 -> R.drawable.sakura_stage_2
            3 -> R.drawable.sakura_stage_3
            4 -> R.drawable.sakura_stage_4
            else -> R.drawable.sakura_stage_0
        }
        imageView.setImageResource(drawableResId)
    }



    private fun setupNavigationBar() {
        // 1. まずナビゲーションバー本体を取得する
        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // 2. ★ここで「色を固定」し「アイコンサイズ」を調整する命令を入れる★
        nav?.itemIconTintList = null // システムによる自動着色（薄暗くする処理）を無効化


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
            val intent = Intent(this, TaskStatsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setNavigationSelection() {
        val selectedNavId = when (currentLayoutId) {
            R.layout.activity_main, R.layout.status_layout -> R.id.nav_home
            else -> R.id.nav_home
        }

        resetNavigationColors()

        val navItemView = findViewById<View>(selectedNavId) ?: return

        val navIcon = navItemView.findViewById<ImageView>(R.id.nav_icon)
        val navLabel = navItemView.findViewById<TextView>(R.id.nav_label)

        // ★文字色とアイコンを「黒」に指定
        val activeColor = Color.BLACK

        navIcon?.setColorFilter(activeColor)
        navLabel?.setTextColor(activeColor)
        navLabel?.setTypeface(null, Typeface.BOLD)

        // ★修正点：Homeが選択された時に水色の背景をセットする
        navItemView.setBackgroundResource(R.drawable.nav_item_background)
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
            val navItemView = findViewById<View>(itemId) ?: continue
            val navIcon = navItemView.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView.findViewById<TextView>(R.id.nav_label)

            navIcon?.setColorFilter(defaultColor)
            navLabel?.setTextColor(defaultColor)
            navLabel?.setTypeface(null, Typeface.NORMAL)

            // 背景をクリアする
            navItemView.background = null
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

    // 鳥の出現ループを管理
    private fun startBirdLoop() {
        stopBirdLoop() // 二重起動防止
        birdRunnable = object : Runnable {
            override fun run() {
                // 現在のレイアウトがstatus_layoutの時だけ鳥を出す
                if (isTimerRunning && currentLayoutId == R.layout.status_layout) {
                    spawnBird()
                }
                // 5秒〜10秒のランダムな間隔で次の鳥を出現させる
                val nextDelay = Random.nextLong(5000, 10000)
                birdHandler.postDelayed(this, nextDelay)
            }
        }
        birdHandler.postDelayed(birdRunnable!!, 3000) // 初回は3秒後に出現
    }

    private fun stopBirdLoop() {
        birdRunnable?.let { birdHandler.removeCallbacks(it) }
    }

    // 鳥を生成してアニメーションさせる
    private fun spawnBird() {
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout) ?: return

        // 1. ImageViewの動的生成
        val bird = ImageView(this)
        val randomId = Random.nextInt(birdResList.size)
        val resId = birdResList[randomId]
        bird.setImageResource(resId)

        // 鳥のサイズ設定 (例: 60dp)
        val size = (100 * resources.displayMetrics.density).toInt()
        val params = ConstraintLayout.LayoutParams(size, size)
        bird.layoutParams = params

        // 2. 出現位置の設定
        val screenWidth = rootLayout.width.toFloat()
        val groundY = rootLayout.height.toFloat() * 0.7f // 画面の下の方(草むら付近)

        // 左から右か、右から左かランダムに決定
        val isLeftToRight = Random.nextBoolean()
        val startX = if (isLeftToRight) -size.toFloat() else screenWidth
        val endX = if (isLeftToRight) screenWidth else -size.toFloat()

        // 逆方向に歩くときは画像を反転させる
        if (!isLeftToRight) {
            if (randomId == 0) bird.scaleX = -1f
        } else {
            if (randomId == 1) bird.scaleX = -1f
        }

        bird.x = startX
        bird.y = groundY + Random.nextInt(-50, 50) // 高さを少しバラけさせる

        rootLayout.addView(bird)

        // 3. アニメーション (水平移動 + ピョコピョコ跳ねる)

        // 横移動
        val moveX = ObjectAnimator.ofFloat(bird, "translationX", startX, endX)
        moveX.duration = Random.nextLong(4000, 7000) // 4〜7秒かけて横切る

        // 縦のピョコピョコ（歩いている感じ）
        val hopY = ObjectAnimator.ofFloat(bird, "translationY", bird.y, bird.y - 30f)
        hopY.duration = 300
        hopY.repeatCount = (moveX.duration / hopY.duration).toInt()
        hopY.repeatMode = ObjectAnimator.REVERSE

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveX, hopY)

        // アニメーション終了後にViewを削除
        animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                rootLayout.removeView(bird)
            }
        })

        animatorSet.start()
    }
}

// --- NotificationWorker (変更なし) ---
class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val intent = Intent(context, Task_MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // セキュリティ上の決まり
        )

        // 通知が消された時にBroadcastを送信するIntentを作成
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java)
        val pendingDismissIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(), // Make requestCode unique
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "eye_rest_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("休憩の時間です")
            .setContentText("目を休ませましょう")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 100, 500)) // Vibrate for 500ms, pause for 100ms, vibrate for 500ms
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingDismissIntent) // ここでセット
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

