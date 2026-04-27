package com.example.myfirstapp

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
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

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

        // Android 13 (TIRAMISU) 以降の場合、通知権限をチェック・要求する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 権限がなければ要求する
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // 権限があればチャンネルを作成
                createNotificationChannel()
            }
        } else {
            // Android 13未満の場合は、チャンネルを作成するだけでよい
            createNotificationChannel()
        }

        // ボタンの取得
        val startButton = findViewById<Button>(R.id.startButton) // 既存のタイマー開始ボタン
        val setupTestButton = findViewById<Button>(R.id.setupTestButton) // 今回追加したテストボタン

        if (startButton == null || setupTestButton == null) {
            println("ボタンが見つかりません！")
            Toast.makeText(this, "ボタンが見つかりません！", Toast.LENGTH_LONG).show()
            return
        }

        // 既存の startButton のリスナー (タイマー画面への遷移を維持)
        startButton.setOnClickListener {
            println("タイマーボタン押された！")
            Toast.makeText(this, "タイマー開始！", Toast.LENGTH_SHORT).show()

            // 休憩通知の WorkManager 予約ロジック
            val workRequest = OneTimeWorkRequestBuilder<RestNotificationWorker>()
                .setInitialDelay(30, TimeUnit.SECONDS) // 30分後に通知
                .build()
            WorkManager.getInstance(this).enqueue(workRequest)

            // BreakTaskActivity への遷移
            val intent = Intent(this, BreakTaskActivity::class.java)
            startActivity(intent)
        }

        // 初期設定テストボタンのリスナー (SetupV1Activityへ遷移)
        setupTestButton.setOnClickListener {
            println("初期設定テストボタン押された！")
            Toast.makeText(this, "初期設定フローへ", Toast.LENGTH_SHORT).show()

            // SetupV1Activity への遷移
            val intent = Intent(this, SetupV1Activity::class.java)
            startActivity(intent)
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
