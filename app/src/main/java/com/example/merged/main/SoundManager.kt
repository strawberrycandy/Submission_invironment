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
        val prefs = context.getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
        val volume = prefs.getInt("seVolume", 50).toFloat() / 100f

        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }
}