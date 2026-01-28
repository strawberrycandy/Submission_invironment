package com.example.merged.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import android.content.Context
import androidx.work.WorkManager
import java.util.UUID

class Task_MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreathTimerScreen(
                onTaskFinished = {
                    val taskPrefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
                    val japanZone = ZoneId.of("Asia/Tokyo")
                    val now = ZonedDateTime.now(japanZone)

                    val dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    val hourStr = now.format(DateTimeFormatter.ofPattern("HH"))

                    val editor = taskPrefs.edit()

                    // 【日グラフ用】
                    val hourKey = "task_count_${dateStr}_${hourStr}"
                    editor.putInt(hourKey, taskPrefs.getInt(hourKey, 0) + 1)

                    // 【週・月グラフ用】
                    val dayKey = "task_count_${dateStr}"
                    editor.putInt(dayKey, taskPrefs.getInt(dayKey, 0) + 1)

                    editor.apply() // 即座に書き込み！

                    // 1. データの読み込みと更新
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val currentCount = prefs.getInt("tasksWithThisCherryBlossom", 0)
                    val newCount = currentCount + 1

                    // 保存（applyで即座に反映）
                    prefs.edit().putInt("tasksWithThisCherryBlossom", newCount).apply()

                    // 全体累計も更新
                    val totalCount = prefs.getInt("taskCountTotal", 0)
                    prefs.edit().putInt("taskCountTotal", totalCount + 1).apply()

                    // Cancel the scheduled eye rest notification
                    val eyeRestWorkId = prefs.getString("eye_rest_work_id", null)
                    eyeRestWorkId?.let {
                        try {
                            WorkManager.getInstance(applicationContext).cancelWorkById(UUID.fromString(it))
                            prefs.edit().remove("eye_rest_work_id").apply() // Remove the stored ID after cancellation
                        } catch (e: IllegalArgumentException) {
                            // Handle cases where the stored ID is not a valid UUID
                            e.printStackTrace()
                        }
                    }

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