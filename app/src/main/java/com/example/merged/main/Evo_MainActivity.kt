package com.example.merged.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import java.util.concurrent.TimeUnit
import com.example.merged.R
import com.example.merged.first_setup.Test

class Evo_MainActivity : AppCompatActivity() {

    // 現在の桜の進化段階を保持する変数 (0: 初期状態, 1: レベル1へ進化完了, ...)
    private var currentSakuraStageIndex: Int = 0

    // AnimationTestActivityからの結果を受け取るためのランチャーを登録
    private val evolutionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // AnimationTestActivityから結果が返ってきた場合
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val finalIndex = data?.getIntExtra(AnimationTestActivity.EXTRA_FINAL_STAGE_INDEX, 0) ?: 0

                // 現在のステージインデックスを更新
                currentSakuraStageIndex = finalIndex

                Toast.makeText(this, "桜がレベル${finalIndex + 1}に進化しました！", Toast.LENGTH_SHORT).show()

                // ボタンの表示を更新
                updateEvolutionButtonText()
            }
        }

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
        setContentView(R.layout.activity_main)

        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)

        updateEvolutionButtonText()

        startEvolutionButton.setOnClickListener {
            // AnimationTestActivity への画面遷移を行う Intent を作成
            val intent = Intent(this, AnimationTestActivity::class.java).apply {
                // 現在の進化インデックスを Intent に詰めて渡す
                putExtra(AnimationTestActivity.EXTRA_FINAL_STAGE_INDEX, currentSakuraStageIndex)
            }

            // Activity を起動し、結果を受け取るようにする
            evolutionResultLauncher.launch(intent)
        }

        // 通知権限チェック
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

        // ボタン取得
        val startButton = findViewById<Button>(R.id.startButton)
        if (startButton == null) {
            println("startButton が null です！ID または setContentView を確認してください")
            Toast.makeText(this, "ボタンが見つかりません！", Toast.LENGTH_LONG).show()
            return
        }

        startButton.setOnClickListener {
            println("ボタン押された！")
            Toast.makeText(this, "ボタン押された！", Toast.LENGTH_SHORT).show()

            // 通知予約
            val workRequest = OneTimeWorkRequestBuilder<RestNotificationWorker>()
                .setInitialDelay(30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(this).enqueue(workRequest)

            // 画面遷移
            val intent = Intent(this, Test::class.java)
            startActivity(intent)
        }
    }

    // 進化ボタンのテキストを更新するメソッド
    private fun updateEvolutionButtonText() {
        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)
        // 桜のステージは全部で5段階 (レベル0からレベル4まで)と仮定
        if (currentSakuraStageIndex < 4) {
            // 次に進化するレベルをテキストに表示 (インデックス+1 = レベル)
            startEvolutionButton.text = "進化アニメーション再生 (レベル${currentSakuraStageIndex + 1}へ)"
            startEvolutionButton.isEnabled = true
        } else {
            startEvolutionButton.text = "最終レベルに到達しました"
            startEvolutionButton.isEnabled = false // 最終レベル到達後は無効化
        }
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

class RestNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(applicationContext, "eye_rest_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("休憩の時間です！")
            .setContentText("30分経ちました。目を休めましょう👀🌸")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val hasNotifyPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasNotifyPermission) {
            NotificationManagerCompat.from(applicationContext).notify(1, notification)
        } else {
            println("通知権限がないため notify をスキップしました")
        }

        return Result.success()
    }
}