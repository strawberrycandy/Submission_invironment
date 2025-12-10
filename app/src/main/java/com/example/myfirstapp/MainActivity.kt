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
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
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

        // res/raw/bgm.mp3 (ファイル名: bgm) を読み込みます
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm)



        // ループ再生の設定
        mediaPlayer?.apply {
            isLooping = true
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
            val intent = Intent(this, BreakTaskActivity::class.java)
            startActivity(intent)
        }
    }
    //  Activityがフォアグラウンドに来たら再生を再開
    override fun onResume() {
        super.onResume()
        mediaPlayer?.run {
            if (!isPlaying) {
                start()
            }
        }
    }

    // ★ 5. Activityがバックグラウンドに回ったら一時停止
    override fun onPause() {
        super.onPause()
        mediaPlayer?.run {
            if (isPlaying) {
                pause()
            }
        }
    }

    // ★ 6. Activityが破棄されるときにリソースを解放
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
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
