package com.example.merged.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.merged.R

private const val USER_NAME_LENGTH_MAX = 8
private const val USER_NAME_LENGTH_MIN = 2

class SettingsActivity : AppCompatActivity() {

    private lateinit var bgmSeekBar: SeekBar
    private lateinit var seSeekBar: SeekBar
    private lateinit var userNameDisplay: TextView
    private lateinit var userNameInput: EditText
    private lateinit var changeNameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        loadSettings()
        setupListeners()
        setupNavigation() // ナビゲーションの設定

        // キーボードを自動で表示
        userNameInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(userNameInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun initializeViews() {
        bgmSeekBar = findViewById(R.id.seekbar_bgm_volume)
        seSeekBar = findViewById(R.id.seekbar_se_volume)
        userNameDisplay = findViewById(R.id.text_view_current_user_name)
        userNameInput = findViewById(R.id.edit_text_new_user_name)
        changeNameButton = findViewById(R.id.button_change_user_name)

        bgmSeekBar.max = 100
        seSeekBar.max = 100
    }

    private fun loadSettings() {
        // 保存先を "app_prefs" に統一
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val currentUserName = prefs.getString("username", "ゲスト")
        val bgmVolume = prefs.getInt("bgmVolume", 50)
        val seVolume = prefs.getInt("seVolume", 50)

        bgmSeekBar.progress = bgmVolume
        seSeekBar.progress = seVolume
        userNameDisplay.text = "現在のユーザー名: $currentUserName"
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val volumeChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 修正点: 0ではなく progress を保存
                    when (seekBar?.id) {
                        R.id.seekbar_bgm_volume -> prefs.edit().putInt("bgmVolume", progress).apply()
                        R.id.seekbar_se_volume -> prefs.edit().putInt("seVolume", progress).apply()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        bgmSeekBar.setOnSeekBarChangeListener(volumeChangeListener)
        seSeekBar.setOnSeekBarChangeListener(volumeChangeListener)

        changeNameButton.setOnClickListener {
            val newName = userNameInput.text.toString().trim()

            if (newName.length < USER_NAME_LENGTH_MIN || newName.length > USER_NAME_LENGTH_MAX) {
                Toast.makeText(this, "ユーザー名は${USER_NAME_LENGTH_MIN}〜${USER_NAME_LENGTH_MAX}文字で入力してください。", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            prefs.edit().putString("username", newName).apply()
            userNameDisplay.text = "現在のユーザー名: $newName"
            userNameInput.setText("")
            Toast.makeText(this, "ユーザー名を「$newName」に変更しました。", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ナビゲーションボタンの処理と現在地の強調 ---
    private fun setupNavigation() {
        val navHome = findViewById<View>(R.id.nav_home)
        val navStatus = findViewById<View>(R.id.nav_status)
        val navResult = findViewById<View>(R.id.nav_result)
        val navSettings = findViewById<View>(R.id.nav_settings)

        // 1. 各画面への遷移設定
        navHome?.setOnClickListener {
            val intent = Intent(this, Home_MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        navStatus?.setOnClickListener {
            startActivity(Intent(this, StatusActivity::class.java))
        }
        navResult?.setOnClickListener {
            startActivity(Intent(this, ResultActivity::class.java))
        }

        // 2. 「SETTING」アイコンと文字の色を強調（現在地表示）
        val settingIcon = navSettings?.findViewById<ImageView>(R.id.nav_icon)
        val settingText = navSettings?.findViewById<TextView>(R.id.nav_label)

        // 強調する色（緑色）を取得
        val activeColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)

        // 色を適用
        settingIcon?.setColorFilter(activeColor)
        settingText?.setTextColor(activeColor)
    }
}