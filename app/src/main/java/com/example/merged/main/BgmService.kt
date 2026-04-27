package com.example.merged.main

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.merged.R

class BgmService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. 音量変更の指示があるか確認
        val volume = intent?.getIntExtra("VOLUME", -1) ?: -1
        if (volume != -1) {
            setMediaPlayerVolume(volume)
        }
        // 2. まだ再生していなければ再生開始
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.evolution_bgm)
            mediaPlayer?.isLooping = true

            // 初回起動時も保存されている音量を適用
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val savedVolume = prefs.getInt("bgmVolume", 50)
            setMediaPlayerVolume(savedVolume)

            mediaPlayer?.start()
        }
        return START_NOT_STICKY // ホームで止める場合は、勝手に再起動しなくて良いのでこちら
    }

    // 音量を設定するヘルパー関数
    private fun setMediaPlayerVolume(volumeProgress: Int) {
        // MediaPlayerの音量は 0.0f 〜 1.0f の float で指定するため変換
        val v = volumeProgress.toFloat() / 100f
        mediaPlayer?.setVolume(v, v)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}