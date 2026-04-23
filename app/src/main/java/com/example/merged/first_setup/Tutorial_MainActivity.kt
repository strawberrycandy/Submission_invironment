package com.example.merged.first_setup

import com.example.merged.main.AnimationTestActivity
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

            // 🔥 フェードアウト完了後に状態初期化を AnimationTestActivity に委任する
            if (isFading) {
                // 初回設定完了のフラグのみ更新
                prefs.edit().putBoolean("is_first_setting", false).apply()

                LaunchedEffect(Unit) {
                    delay(3000) // フェードアウト演出を待つ
                    // AnimationTestActivity を経由してホームへ遷移
                    startActivity(
                        Intent(
                            this@Tutorial_MainActivity,
                            AnimationTestActivity::class.java
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
