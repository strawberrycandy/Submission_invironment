package com.example.myfirstapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    // 通知を区別するためのリクエストコード
    companion object {
        const val REQUEST_CODE = 100
    }

    override fun doWork(): Result {

        // 1. タップで起動したいActivityのIntentを作成
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            // アプリがバックグラウンドや終了状態でも正しく起動するためのフラグ
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. IntentをPendingIntentでラップする
        // FLAG_IMMUTABLEは Android 12 (API 31)以降で必須です
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "eye_rest_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("休憩の時間です！")
            .setContentText("30分経ちました。目を休めましょう👀🌸")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // 3. PendingIntentを通知に設定
            .setContentIntent(pendingIntent) // ★ここを追加★
            .setAutoCancel(true) // タップ後に通知を自動的に消去
            .build()

        // ... (通知権限チェックと通知実行のロジックはそのまま) ...

        val hasNotifyPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasNotifyPermission) {
            // 通知IDはここでは 1 を使用
            NotificationManagerCompat.from(applicationContext).notify(1, notification)
        } else {
            println("通知権限がないため notify をスキップしました")
        }

        return Result.success()
    }
}