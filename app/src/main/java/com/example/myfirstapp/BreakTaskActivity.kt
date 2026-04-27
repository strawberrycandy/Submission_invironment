package com.example.myfirstapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class BreakTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_task)
    }
    // ★ onResumeでサービスを開始（BGM再生開始）
    override fun onResume() {
        super.onResume()
        val bgmIntent = Intent(this, BackgroundMusicService::class.java)
        startService(bgmIntent)
    }
    // ★ onPauseでサービスを停止（BGM一時停止/停止）
    override fun onPause() {
        super.onPause()
        val bgmIntent = Intent(this, BackgroundMusicService::class.java)
        stopService(bgmIntent)
    }
}
