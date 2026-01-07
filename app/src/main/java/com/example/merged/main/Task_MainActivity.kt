package com.example.merged.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.merged.main.BreathTimerScreen
import android.content.Intent


class Task_MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        super.onCreate(savedInstanceState)

        setContent {
            BreathTimerScreen(
                onTaskFinished = {
                    val intent = Intent(this, AnimationTestActivity::class.java)
                    intent.putExtra("EXTRA_FINAL_STAGE_INDEX",
                        prefs.getInt("cherryBlossomGrowthStage", 0))
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}
