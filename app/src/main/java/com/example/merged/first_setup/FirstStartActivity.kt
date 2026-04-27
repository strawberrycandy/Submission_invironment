package com.example.merged.first_setup

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.merged.R

// --- 定義をファイル先頭に移動 (コードの整理のため) ---
const val CHANNEL_ID = "eye_rest_channel"
const val CHANNEL_NAME = "休憩通知チャンネル"
const val NOTIFICATION_ID = 1
const val DELAY_MINUTES: Long = 30 // 休憩通知までの時間 (分)
private const val TAG = "FirstStartActivity"

class FirstStartActivity : AppCompatActivity() {

    // 通知権限リクエストランチャー
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Log.dでより開発者向けのログ出力
                Log.d(TAG, "通知許可が与えられました")
            } else {
                Log.d(TAG, "通知許可が拒否されました")
                // 許可が拒否されたことをユーザーに伝えるトースト
                Toast.makeText(this, "通知を許可しないと休憩のお知らせが届きません。", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // R.layout.activity_main の代わりに、ご提示の画面に対応するレイアウトIDを指定してください。
        // 例: setContentView(R.layout.activity_initial_screen)
        setContentView(R.layout.activity_first_start)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_setting", true).apply()

        // 通知権限チェックとリクエスト
        requestNotificationPermission()

        // 通知チャンネルの作成
        createNotificationChannel()

        // ボタン取得
        // R.id.startButton の代わりに、ご提示の画面のボタンID (例: R.id.start_button) を指定してください。
        val startButton = findViewById<Button>(R.id.start_button)

        // ボタンが見つからなかった場合の処理を強化 (Log.eでエラーとして出力)
        if (startButton == null) {
            Log.e(TAG, "ID: R.id.startButton のボタンが見つかりませんでした。XMLレイアウトを確認してください。")
            Toast.makeText(this, "ボタンが見つかりません！", Toast.LENGTH_LONG).show()
            return
        }

        startButton.setOnClickListener {
            // 画面遷移
            // Intent先のActivity名が正しいか確認してください
            val intent = Intent(this, TermsAndConditionsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 許可がまだない場合はリクエスト
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, // 定数を使用
                CHANNEL_NAME, // 定数を使用
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "目を休めるための休憩時間をお知らせする通知です。"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

// --- RestNotificationWorkerクラスは変更なしでOK ---
class RestNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    // Log.dを使うことで、アプリのデバッグレベルのログとして確認できます
    private val workerTag = "NotificationWorker"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID) // 定数を使用
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // アイコンは必ず自分で用意したものを設定してください
            .setContentTitle("休憩の時間です！")
            .setContentText("${DELAY_MINUTES}分経ちました。目を休めましょう👀🌸") // 定数を使用
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Tiramisu (API 33)以降での通知権限の再チェック
        val hasNotifyPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12以前は実行時に権限は不要
        }

        if (hasNotifyPermission) {
            // notifyのIDにも定数を使用
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
            Log.d(workerTag, "通知を送信しました (ID: $NOTIFICATION_ID)")
        } else {
            Log.w(workerTag, "通知権限がないため notify をスキップしました")
        }

        return Result.success()
    }
}