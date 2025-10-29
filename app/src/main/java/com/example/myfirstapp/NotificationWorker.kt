package com.example.myfirstapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
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