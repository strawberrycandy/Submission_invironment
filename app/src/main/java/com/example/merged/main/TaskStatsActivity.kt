package com.example.merged.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.merged.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TaskStatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private val japanZone = ZoneId.of("Asia/Tokyo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_status)

        barChart = findViewById(R.id.barChart)
        barChart.setNoDataText("")

        // デバッグ用：現在の保存状態をログに出力
        checkSavedData()

        // ×ボタンの処理
        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            val intent = Intent(this, Home_MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnDay).setOnClickListener { showDaily() }
        findViewById<Button>(R.id.btnWeek).setOnClickListener { showWeekly() }
        findViewById<Button>(R.id.btnMonth).setOnClickListener { showMonthly() }

        // 初回表示
        showDaily()


        // ナビゲーションの初期化
        setupNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        // 画面に戻ってくるたびに「今日」のデータを読み直す
        showDaily()
    }

    private fun checkSavedData() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val allEntries = prefs.all
        if (allEntries.isEmpty()) {
            android.util.Log.d("DEBUG_PREFS_ALL", "保存されているデータは【空】です")
        } else {
            for ((key, value) in allEntries) {
                android.util.Log.d("DEBUG_PREFS_ALL", "保存済みデータ -> Key: $key, Value: $value")
            }
        }
    }

    private fun showDaily() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = ZonedDateTime.now(japanZone)
        val todayStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        val entries = (0 until 8).map { i ->
            var sum = 0
            for (h in 0 until 3) {
                val hour = i * 3 + h
                val hourStr = String.format(Locale.getDefault(), "%02d", hour)
                val key = "task_count_${todayStr}_$hourStr"
                sum += prefs.getInt(key, 0)
            }
            BarEntry(i.toFloat(), sum.toFloat())
        }

        val labels = listOf("0-3", "3-6", "6-9", "9-12", "12-15", "15-18", "18-21", "21-0")
        setBarChart(entries, "今日の記録 (時)", labels, isScrollable = false)
    }

    private fun showWeekly() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = ZonedDateTime.now(japanZone)
        val today = now.toLocalDate()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())

        val entries = (0 until 7).map { i ->
            val day = monday.plusDays(i.toLong())
            val key = "task_count_${day.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            val count = prefs.getInt(key, 0)
            BarEntry(i.toFloat(), count.toFloat())
        }

        val labels = listOf("月", "火", "水", "木", "金", "土", "日")
        setBarChart(entries, "今週の状況", labels, isScrollable = false)
    }

    private fun showMonthly() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = ZonedDateTime.now(japanZone)
        val today = now.toLocalDate()
        val daysInMonth = today.lengthOfMonth()

        val entries = (0 until daysInMonth).map { i ->
            val day = today.withDayOfMonth(i + 1)
            val key = "task_count_${day.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            val count = prefs.getInt(key, 0)
            BarEntry(i.toFloat(), count.toFloat())
        }

        val labels = (1..daysInMonth).map { "${it}日" }
        setBarChart(entries, "${today.monthValue}月の記録", labels, isScrollable = true)
    }

    private fun setBarChart(entries: List<BarEntry>, label: String, xLabels: List<String>, isScrollable: Boolean) {
        // 常に表示状態にする
        barChart.visibility = View.VISIBLE
        barChart.setNoDataText("データがありません") // データ0の時のテキスト

        val totalValue = entries.sumOf { it.y.toDouble() }

        if (totalValue == 0.0) {
            barChart.clear()
            // データがなくてもナビゲーション（緑の背景）だけは絶対に描画する
            setNavigationSelection()
            return
        }

        val dataSet = BarDataSet(entries, label).apply {
            valueTextSize = 10f
            color = "#FFC0CB".toColorInt()
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.6f
            }

            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        // インデックスが範囲内にあるか厳密にチェック
                        return if (index >= 0 && index < xLabels.size) xLabels[index] else ""
                    }
                }
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)

                // ★グリッドのズレ対策★
                granularity = 1f // 1単位ごとにラベルを表示
                isGranularityEnabled = true
                labelCount = xLabels.size // ラベルの数を配列のサイズに強制固定
                setCenterAxisLabels(false) // ラベルを棒の真下に配置
            }

            // 余計な表示をオフ
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            setFitBars(true)

            if (isScrollable) {
                setVisibleXRangeMaximum(7f)
                moveViewToX(entries.size.toFloat())
            } else {
                fitScreen()
            }

            animateY(800)
            invalidate()
        }

        // 最後に必ずナビゲーションを塗る
        setNavigationSelection()
    }

    private fun setupNavigationBar() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        nav?.itemIconTintList = null
        nav?.itemTextColor = null

        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            startActivity(Intent(this, Home_MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            startActivity(Intent(this, StatusActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    private fun setNavigationSelection() {
        val navContainer = findViewById<View>(R.id.navigation_bar) ?: return
        navContainer.bringToFront()

        val navItems = listOf(R.id.nav_home, R.id.nav_status, R.id.nav_settings, R.id.nav_result)
        val activeColor = Color.parseColor("#00008B")
        val defaultColor = Color.parseColor("#A9A9A9")

        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId) ?: continue
            val navIcon = navItemView.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView.findViewById<TextView>(R.id.nav_label)

            if (itemId == R.id.nav_result) {
                navIcon?.imageTintList = ColorStateList.valueOf(activeColor)
                navLabel?.setTextColor(activeColor)
                navLabel?.setTypeface(null, Typeface.BOLD)
                navItemView.setBackgroundResource(R.drawable.nav_item_background)
            } else {
                navIcon?.imageTintList = ColorStateList.valueOf(defaultColor)
                navLabel?.setTextColor(defaultColor)
                navLabel?.setTypeface(null, Typeface.NORMAL)
                navItemView.background = null
            }
        }
    }
}