package com.example.myfirstapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
// ★追加: 必要なウィジェットとユーティリティのインポート
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat // ★追加
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
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

        // 最初にスタート画面のレイアウトを設定
        setContentView(R.layout.activity_main)

        // 通知権限チェックとチャンネル作成
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

        // Button取得
        val startButton = findViewById<Button>(R.id.startButton)

        startButton?.setOnClickListener {
            // ★★★ 画面切り替え処理 ★★★
            setContentView(R.layout.status_layout)

            Toast.makeText(this, "ステータス画面に遷移しました", Toast.LENGTH_SHORT).show()

            scheduleNotification()

            // ナビゲーションバーの「STATUS」項目をハイライトする処理を呼び出す
            setNavigationSelection()
        }
    }

    // 通知予約関数
    private fun scheduleNotification() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    // ナビゲーションバーの選択状態を更新する関数
    private fun setNavigationSelection() {
        // status_layout内にナビゲーションバーが存在することを前提とする
        val navStatus = findViewById<View>(R.id.nav_status)

        navStatus?.let {
            // 背景色をnav_select_greenに設定
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.nav_select_green))

            // ラベルとアイコンを取得
            val statusLabel = it.findViewById<TextView>(R.id.nav_label)
            val statusIcon = it.findViewById<ImageView>(R.id.nav_icon)

            // 色を白に設定
            statusLabel?.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            statusIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
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

// WorkManagerがバックグラウンドで実行する処理を定義するクラス (変更なし)
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