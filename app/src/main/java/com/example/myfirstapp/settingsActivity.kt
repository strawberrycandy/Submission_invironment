package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager // ★この行が重要
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.text.trim

private const val PREF_FILE_NAME = "GameSettings"
private const val KEY_BGM_VOLUME = "bgmVolume"
private const val KEY_SE_VOLUME = "seVolume"
private const val KEY_USER_NAME = "userName"

private const val BGM_VOLUME_MAX = 100
private const val SE_VOLUME_MAX = 100
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

        // ★★★ 設定画面を開いた瞬間にキーボードを自動で表示するコード ★★★
        userNameInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(userNameInput, InputMethodManager.SHOW_IMPLICIT)
        // ★★★ ここまで ★★★
    }

    private fun initializeViews() {
        bgmSeekBar = findViewById(R.id.seekbar_bgm_volume)
        seSeekBar = findViewById(R.id.seekbar_se_volume)
        userNameDisplay = findViewById(R.id.text_view_current_user_name)
        userNameInput = findViewById(R.id.edit_text_new_user_name)
        changeNameButton = findViewById(R.id.button_change_user_name)

        bgmSeekBar.max = BGM_VOLUME_MAX
        seSeekBar.max = SE_VOLUME_MAX
    }

    private fun loadSettings() {
        val sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE)

        val bgmVolume = sharedPref.getInt(KEY_BGM_VOLUME, 50)
        bgmSeekBar.progress = bgmVolume

        val seVolume = sharedPref.getInt(KEY_SE_VOLUME, 50)
        seSeekBar.progress = seVolume

        val currentUserName = sharedPref.getString(KEY_USER_NAME, "名無し") ?: "名無し"
        userNameDisplay.text = "現在のユーザー名: $currentUserName"
    }

    private fun setupListeners() {
        val editor = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE).edit()

        val volumeChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar?.id) {
                    R.id.seekbar_bgm_volume -> editor.putInt(KEY_BGM_VOLUME, progress).apply()
                    R.id.seekbar_se_volume -> editor.putInt(KEY_SE_VOLUME, progress).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        bgmSeekBar.setOnSeekBarChangeListener(volumeChangeListener)
        seSeekBar.setOnSeekBarChangeListener(volumeChangeListener)

        changeNameButton.setOnClickListener{
            val newName = userNameInput.text.toString().trim()

            if (newName.length < USER_NAME_LENGTH_MIN || newName.length > USER_NAME_LENGTH_MAX) {
                Toast.makeText(this@SettingsActivity, "ユーザー名は${USER_NAME_LENGTH_MIN}〜${USER_NAME_LENGTH_MAX}文字で入力してください。", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            editor.putString(KEY_USER_NAME, newName).apply()

            userNameDisplay.text = "現在のユーザー名: $newName"
            userNameInput.setText("")

            Toast.makeText(this@SettingsActivity, "ユーザー名を「$newName」に変更しました。", Toast.LENGTH_SHORT).show()
        }
    }
}