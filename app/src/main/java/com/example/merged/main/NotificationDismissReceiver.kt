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
        }
    }
}
