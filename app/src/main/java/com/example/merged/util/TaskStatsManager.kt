package com.example.merged.util
import android.content.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

object TaskStatsManager {

    private const val PREF_NAME = "task_prefs"

    fun saveTaskCompleted(context: Context) {
        /*
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val now = LocalDateTime.now()
        val editor = prefs.edit()

        // 日：1時間ごと
        val hourKey = now.format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HH")
        )
        editor.putInt(
            "task_count_$hourKey",
            prefs.getInt("task_count_$hourKey", 0) + 1
        )

        // 週：1日ごと
        val dayKey = now.format(
            DateTimeFormatter.ofPattern("yyyyMMdd")
        )
        editor.putInt(
            "task_count_$dayKey",
            prefs.getInt("task_count_$dayKey", 0) + 1
        )

        // 月：1週間ごと
        val weekFields = WeekFields.ISO
        val week = now.get(weekFields.weekOfWeekBasedYear())
        val year = now.get(weekFields.weekBasedYear())
        val weekKey = "task_count_${year}_w$week"

        editor.putInt(
            weekKey,
            prefs.getInt(weekKey, 0) + 1
        )

        editor.apply()
        */
    }
}