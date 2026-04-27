package com.example.merged.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.merged.util.BugManager

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationDismiss", "Notification dismissed. Attempting to add bug.")
        if (context != null) {
            // 通知が無視されたので、虫を追加する
            BugManager.addBug(context)

            //  ★花の状態を悪化させる (0:良い -> 1:普通 -> 2:悪い)
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentStatus = prefs.getInt("cherryBlossomStatus", 0)

            // 最大値が2(悪い)なので、それ以下の時だけ増やす
            if (currentStatus < 2) {
                val newStatus = currentStatus + 1
                prefs.edit().putInt("cherryBlossomStatus", newStatus).apply()
                Log.d("NotificationDismiss", "Flower status worsened to: $newStatus")
            }
        }
    }
}
