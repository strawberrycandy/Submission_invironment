package com.example.merged

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.first_setup.FirstStartActivity
import com.example.merged.main.Home_MainActivity
import android.util.Log
import com.example.merged.main.BgmService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // first.xml を読み込む
        setContentView(R.layout.activity_start)

        // Button を取得
        val startButton = findViewById<Button>(R.id.startButton)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val is_first_setting = prefs.getBoolean("is_first_setting", true)
        // val is_first_setting = true
        // ボタン押下で FirstStartActivity.kt に遷移
        startButton.setOnClickListener {
            val intent = if (is_first_setting) {
                Intent(this, FirstStartActivity::class.java)
            } else {
                Intent(this, Home_MainActivity::class.java)
            }
            startActivity(intent)
        }
        // BGMの監視設定
        val bgmIntent = Intent(this, BgmService::class.java)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // アプリが画面に出てきたらBGMを開始
                startService(bgmIntent)
                Log.d(TAG, "BGM Started")
            }
            override fun onStop(owner: LifecycleOwner) {
                // アプリがバックグラウンドに隠れたらBGMを停止
                stopService(bgmIntent)
                Log.d(TAG, "BGM Stopped")
            }
        })
    }
}