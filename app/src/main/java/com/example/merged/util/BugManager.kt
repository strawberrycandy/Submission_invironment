package com.example.merged.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object BugManager {

    private const val PREFS_NAME = "bug_manager_prefs"
    private const val KEY_BUG_COUNT = "bug_count"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addBug(context: Context) {
        val prefs = getSharedPreferences(context)
        val currentBugs = getBugCount(context)
        val newCount = currentBugs + 1
        prefs.edit().putInt(KEY_BUG_COUNT, newCount).apply()
        Log.d("BugManager", "Bug added. Current: $currentBugs -> New: $newCount")
    }

    fun getBugCount(context: Context): Int {
        val prefs = getSharedPreferences(context)
        val count = prefs.getInt(KEY_BUG_COUNT, 0)
        Log.d("BugManager", "Retrieving bug count: $count")
        return count
    }

    // 将来的に虫を減らす機能が必要になった場合に使用
    fun removeBugs(context: Context, count: Int) {
        val prefs = getSharedPreferences(context)
        val currentBugs = getBugCount(context)
        val newCount = maxOf(0, currentBugs - count)
        prefs.edit().putInt(KEY_BUG_COUNT, newCount).apply()
        Log.d("BugManager", "Bugs removed. Current: $currentBugs -> New: $newCount (removed $count)")
    }

    fun resetBugs(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putInt(KEY_BUG_COUNT, 0).apply()
        Log.d("BugManager", "Bug count reset to 0.")
    }
}
