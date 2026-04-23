package com.example.merged.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class Task_MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        setContent {
            BreathTimerScreen(
                onTaskFinished = {
                    // アニメーション画面へ遷移するだけにする
                    val intent = Intent(this, AnimationTestActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}