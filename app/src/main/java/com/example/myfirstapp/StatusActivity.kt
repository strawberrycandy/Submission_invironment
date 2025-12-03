package com.example.myfirstapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // ▼▼▼ ここから下の処理をすべて追加 ▼▼▼

        // ナビゲーションバーのボタン処理を設定
        setupNavigationBar()
        // ナビゲーションバーのハイライトを設定
        setNavigationSelection()
    }

    /**
     * ナビゲーションバーのクリック処理を設定する
     */
    private fun setupNavigationBar() {
        // HOMEボタンが押されたら、この画面を閉じて前の画面(MainActivity)に戻る
        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            finish() // 現在のActivityを終了する
        }

        // STATUSボタンは現在の画面なので、何もしない
        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            // 何もしない
        }

        // 他のボタンも同様に、押されたら対応するActivityに遷移する処理を記述できます
        // findViewById<View>(R.id.nav_favorite)?.setOnClickListener { ... }
        // findViewById<View>(R.id.nav_settings)?.setOnClickListener { ... }
        // findViewById<View>(R.id.nav_result)?.setOnClickListener { ... }
    }

    /**
     * ナビゲーションバーの選択状態（ハイライト）を設定する
     */
    private fun setNavigationSelection() {
        // この画面では常に "STATUS" を選択状態にする
        val selectedNavId = R.id.nav_status

        // すべてのボタンの色を一旦リセット
        resetNavigationColors()

        // 選択されたボタンをハイライトする
        val navItemView = findViewById<View>(selectedNavId)
        val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
        val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)

        // 選択状態の色（緑色）を設定
        val activeColor = resources.getColor(android.R.color.holo_green_dark, theme)
        navIcon?.setColorFilter(activeColor)
        navLabel?.setTextColor(activeColor)
    }

    /**
     * すべてのナビゲーションボタンの色をデフォルト（灰色）に戻す
     */
    private fun resetNavigationColors() {
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_status,
            R.id.nav_favorite,
            R.id.nav_settings,
            R.id.nav_result
        )

        val defaultColor = resources.getColor(android.R.color.darker_gray, theme)

        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId)
            val navIcon = navItemView?.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView?.findViewById<TextView>(R.id.nav_label)

            navIcon?.setColorFilter(defaultColor)
            navLabel?.setTextColor(defaultColor)
        }
    }
}
