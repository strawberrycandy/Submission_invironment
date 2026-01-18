package com.example.merged.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.merged.R
import com.example.merged.first_setup.Test

class StatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        displayCurrentStatus()

        // 画面上部の「ばつ」アイコンのクリック処理
        findViewById<ImageView>(R.id.back_button_status)?.setOnClickListener {
            finish() // 現在のActivityを終了し、ホーム画面に戻る
        }

        setupNavigationBar()
        setNavigationSelection()
    }

    private fun displayCurrentStatus() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // 変数名の訂正に基づき取得
        val growth = prefs.getInt("cherryBlossomGrowthStage", 0)
        val flower = prefs.getInt("cherryBlossomStatus", 0)
        val soil = prefs.getInt("soilStatus", 0)

        // 成長段階の反映 (Lv.x)
        val growthText = getString(R.string.growth_level_format, growth)
        findViewById<TextView>(R.id.growth_stage_text)?.text = growthText

        // 数値を文字に変換するヘルパー関数
        fun convertRating(value: Int): String {
            return when (value) {
                2 -> "普通"
                1 -> "悪い"
                else -> "悪い" // 0や予期せぬ値
            }
        }

        // 花と土の状態を反映
        findViewById<TextView>(R.id.flower_status_text)?.text = convertRating(flower)
        findViewById<TextView>(R.id.soil_status_text)?.text = convertRating(soil)
    }

    private fun setupNavigationBar() {
        // HOMEボタンが押されたら、この画面を閉じて前の画面(MainActivity)に戻る
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            finish()
        }

        // STATUSボタンは現在の画面なので、何もしない
        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            // 何もしない
        }


        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.nav_result)?.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setNavigationSelection() {
        val selectedNavId = R.id.nav_status

        resetNavigationColors()

        val navItemView = findViewById<View>(selectedNavId)
        val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
        val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)

        val activeColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        navIcon?.setColorFilter(activeColor)
        navLabel?.setTextColor(activeColor)
    }

    private fun resetNavigationColors() {
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_status,
            R.id.nav_settings,
            R.id.nav_result
        )

        val defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray)

        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId)
            val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)

            navIcon?.setColorFilter(defaultColor)
            navLabel?.setTextColor(defaultColor)
        }
    }
}