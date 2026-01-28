package com.example.merged.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class Task_MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreathTimerScreen(
                onTaskFinished = {
                    // 統計用のデータを保存する
                    com.example.merged.util.TaskStatsManager.saveTaskCompleted(this@Task_MainActivity)
                    // 1. データの読み込みと更新
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val currentCount = prefs.getInt("tasksWithThisCherryBlossom", 0)
                    val newCount = currentCount + 1

                    // 保存（applyで即座に反映）
                    prefs.edit().putInt("tasksWithThisCherryBlossom", newCount).apply()

                    // 全体累計も更新
                    val totalCount = prefs.getInt("taskCountTotal", 0)
                    prefs.edit().putInt("taskCountTotal", totalCount + 1).apply()

                    // 2. アニメーション画面（AnimationTestActivity）への遷移
                    // this@Task_MainActivity と書くことで、Compose内から確実に画面を切り替えます
                    val intent = Intent(this@Task_MainActivity, AnimationTestActivity::class.java)

                    // 次の画面に「今回のステージ」を渡す
                    val currentStage = (newCount / 2).coerceAtMost(4)
                    intent.putExtra(AnimationTestActivity.EXTRA_FINAL_STAGE_INDEX, currentStage)

                    startActivity(intent)
                    finish() // この画面は閉じる
                }
            )
        }
    }
}