package com.example.merged.main

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R

object SoundManager {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0

    // 最初に一度だけ呼ぶ（初期化）
    fun init(context: Context) {
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()

            // ファイル名はご自身が用意したものに変更してください
            soundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
        }
    }

    // どこからでも呼べる再生関数
    fun playSE(context: Context) {
        // 保存先 "app_prefs" から最新の seVolume を取得
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val volumeValue = prefs.getInt("seVolume", 50)

        // 0-100 の値を 0.0-1.0 に変換
        val volume = volumeValue.toFloat() / 100f

        // 最新の音量で再生
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }
}