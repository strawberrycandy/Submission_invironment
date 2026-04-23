package com.example.merged.main

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
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

        // --- ナビゲーション設定 ---
        setupNavigationBar()
        setNavigationSelection()

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
                    when (seekBar?.id) {
                        R.id.seekbar_bgm_volume -> {
                            prefs.edit().putInt("bgmVolume", progress).apply()
                            val intent = Intent(this@SettingsActivity, BgmService::class.java)
                            intent.putExtra("VOLUME", progress)
                            startService(intent)
                        }
                        R.id.seekbar_se_volume -> {
                            prefs.edit().putInt("seVolume", progress).apply()
                        }
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

    // --- ナビゲーションバーの処理（StatusActivityと統一） ---
    private fun setupNavigationBar() {
        val nav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // 紫色の干渉をブロック
        nav?.itemIconTintList = null
        nav?.itemTextColor = null

        findViewById<View>(R.id.nav_home)?.setOnClickListener {
            val intent = Intent(this, Home_MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        findViewById<View>(R.id.nav_status)?.setOnClickListener {
            startActivity(Intent(this, StatusActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.nav_result)?.setOnClickListener {
            startActivity(Intent(this, ResultActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            // 現在地
        }
    }

    private fun setNavigationSelection() {
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_status,
            R.id.nav_settings,
            R.id.nav_result
        )

        val activeColor = Color.parseColor("#00008B") // 濃い青
        val defaultColor = Color.parseColor("#A9A9A9") // グレー

        for (itemId in navItems) {
            val navItemView = findViewById<View>(itemId) ?: continue
            val navIcon = navItemView.findViewById<ImageView>(R.id.nav_icon)
            val navLabel = navItemView.findViewById<TextView>(R.id.nav_label)

            if (itemId == R.id.nav_settings) {
                // Settings（現在地）の設定
                navIcon?.imageTintList = ColorStateList.valueOf(activeColor)
                navLabel?.setTextColor(activeColor)
                navLabel?.setTypeface(null, Typeface.BOLD)
                navItemView.setBackgroundResource(R.drawable.nav_item_background)
            } else {
                // 他の設定
                navIcon?.imageTintList = ColorStateList.valueOf(defaultColor)
                navLabel?.setTextColor(defaultColor)
                navLabel?.setTypeface(null, Typeface.NORMAL)
                navItemView.background = null
            }
        }
    }
}