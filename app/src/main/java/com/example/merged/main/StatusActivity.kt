package com.example.merged.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R

class StatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        displayCurrentStatus()

        // 画面上部の「ばつ」アイコンのクリック処理
        findViewById<ImageView>(R.id.back_button_status)?.setOnClickListener {
            finish()
        }

        setupNavigationBar()
        setNavigationSelection() // ここで見た目を整える
    }

    private fun displayCurrentStatus() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // 変数名の訂正に基づき取得
        val growth = prefs.getInt("cherryBlossomGrowthStage", 0)
        val flower = prefs.getInt("cherryBlossomStatus", 0)

        findViewById<TextView>(R.id.sakura_type_text)?.text = "ソメイヨシノ"

        // 成長段階の反映 (Lv.x)
        val growthText = getString(R.string.growth_level_format, growth)
        findViewById<TextView>(R.id.growth_stage_text)?.text = growthText

        // ★判定ロジックの修正
        fun convertRating(value: Int): String {
            return when (value) {
                0 -> "良い"  // 最初（0）は「良い」から始まる
                1 -> "普通"
                2 -> "悪い"
                else -> "良い"
            }
        }

        // 花と土の状態を反映
        findViewById<TextView>(R.id.flower_status_text)?.text = convertRating(flower)

    }

    private fun setupNavigationBar() {
        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // システムの自動着色をオフにする（紫になるのを防ぐ）
        nav?.itemIconTintList = null
        nav?.itemTextColor = null

        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            // 何もしない（現在地）
        }

        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish() // 画面を切り替えるときに今の画面を閉じる
        }

        findViewById<View>(R.id.nav_result)?.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
            finish() // 画面を切り替えるときに今の画面を閉じる
        }
    }

    private fun setNavigationSelection() {
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_status,
            R.id.nav_settings,
            R.id.nav_result
        )

        // 色の定義
        val activeColor = Color.parseColor("#00008B") // 濃い青
        val defaultColor = Color.parseColor("#A9A9A9") // グレー

        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId) ?: continue
            val navIcon = navItemView.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView.findViewById<TextView>(R.id.nav_label)

            if (itemId == R.id.nav_status) {
                // 【現在地（Status）の設定】
                navIcon?.imageTintList = ColorStateList.valueOf(activeColor)
                navLabel?.setTextColor(activeColor)
                navLabel?.setTypeface(null, Typeface.BOLD) // 太字

                // ★水色のカプセル背景を表示
                navItemView.setBackgroundResource(R.drawable.nav_item_background)
            } else {
                // 【それ以外（グレー）の設定】
                navIcon?.imageTintList = ColorStateList.valueOf(defaultColor)
                navLabel?.setTextColor(defaultColor)
                navLabel?.setTypeface(null, Typeface.NORMAL) // 通常

                // 背景を消す
                navItemView.background = null
            }
        }
    }
}