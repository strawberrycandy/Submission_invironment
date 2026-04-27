package com.example.merged.main

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

class TaskStatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks_status)

        barChart = findViewById(R.id.barChart)

        val btnDay = findViewById<Button>(R.id.btnDay)
        val btnWeek = findViewById<Button>(R.id.btnWeek)
        val btnMonth = findViewById<Button>(R.id.btnMonth)

        btnDay.setOnClickListener { showDaily() }
        btnWeek.setOnClickListener { showWeekly() }
        btnMonth.setOnClickListener { showMonthly() }

        // 初期表示
        showDaily()
    }

    /* =====================
       日：直近24時間（1時間ごと）
       ===================== */
    private fun showDaily() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH")

        val entries = (0 until 24).map { i ->
            val time = now.minusHours((23 - i).toLong())
            val key = "task_count_${time.format(formatter)}"
            val count = prefs.getInt(key, 0)
            BarEntry(i.toFloat(), count.toFloat())
        }

        setBarChart(entries, "直近24時間")
    }

    /* =====================
       週：直近7日（1日ごと）
       ===================== */
    private fun showWeekly() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        val entries = (0 until 7).map { i ->
            val day = now.minusDays((6 - i).toLong())
            val key = "task_count_${day.format(formatter)}"
            val count = prefs.getInt(key, 0)
            BarEntry(i.toFloat(), count.toFloat())
        }

        setBarChart(entries, "直近7日間")
    }

    /* =====================
       月：直近7週間（1週間ごと）
       ===================== */
    private fun showMonthly() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = LocalDate.now()
        val weekFields = WeekFields.ISO

        val entries = (0 until 7).map { i ->
            val weekDate = now.minusWeeks((6 - i).toLong())
            val week = weekDate.get(weekFields.weekOfWeekBasedYear())
            val year = weekDate.get(weekFields.weekBasedYear())
            val key = "task_count_${year}_w$week"
            val count = prefs.getInt(key, 0)
            BarEntry(i.toFloat(), count.toFloat())
        }

        setBarChart(entries, "直近7週間")
    }

    /* =====================
       棒グラフ共通設定
       ===================== */
    private fun setBarChart(entries: List<BarEntry>, label: String) {
        val dataSet = BarDataSet(entries, label).apply {
            valueTextSize = 12f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            axisRight.isEnabled = false
            invalidate()
        }
    }
}
