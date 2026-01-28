package com.example.merged.main

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TaskStatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private val japanZone = ZoneId.of("Asia/Tokyo")

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

        showDaily()
    }

    private fun showDaily() {
        val prefs = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val now = ZonedDateTime.now(japanZone)
        val todayStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        val entries = (0 until 8).map { i ->
            var sum = 0
            for (h in 0 until 3) {
                val hour = i * 3 + h
                val hourStr = String.format("%02d", hour)
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
        val dataSet = BarDataSet(entries, label).apply {
            valueTextSize = 10f
            color = android.graphics.Color.parseColor("#FFC0CB")
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in xLabels.indices) xLabels[index] else ""
                    }
                }
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = if (isScrollable) 7 else xLabels.size
            }

            if (isScrollable) {
                setVisibleXRangeMaximum(7f)
                moveViewToX(entries.size.toFloat())
            } else {
                fitScreen()
            }

            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }
}