package com.example.merged.first_setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.example.merged.main.Home_MainActivity
import kotlinx.coroutines.delay

class Tutorial_MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

            var currentPage by remember { mutableStateOf(1) }
            val maxPage = tutorialPages.size
            val userName = prefs.getString("username", "ゲスト") ?: "ゲスト"

            var isFading by remember { mutableStateOf(false) }

            // 🔥 フェードアウト完了後に遷移
            if (isFading) {
                prefs.edit().putBoolean("is_first_setting", false).apply()
                prefs.edit().putInt("cherryBlossomGrowthStage", 0).apply()
                prefs.edit().putInt("cherryBlossomStatus", 0).apply()
                prefs.edit().putInt("soilStatus", 0).apply()
                prefs.edit().putInt("bgmVolume", 30).apply()
                prefs.edit().putInt("seVolume", 30).apply()

                prefs.edit().putInt("tasksWithThisCherryBlossom", 0).apply()
                prefs.edit().putInt("taskCountTotal", 0).apply()


                LaunchedEffect(Unit) {
                    delay(3000)
                    startActivity(
                        Intent(
                            this@Tutorial_MainActivity,
                            Home_MainActivity::class.java
                        )
                    )


                    finish()
                }
            }

            Surface(color = MaterialTheme.colorScheme.background) {
                TutorialScreen(
                    currentPage = currentPage,
                    userName = userName,
                    isFading = isFading,
                    onNextPage = {
                        if (currentPage < maxPage) {
                            currentPage++
                        } else {
                            // 🔥 「はじめる」押下
                            isFading = true
                        }
                    }
                )
            }
        }
    }
}
