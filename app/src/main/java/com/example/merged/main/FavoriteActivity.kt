package com.example.merged.main

import android.os.Bundle
import android.widget.ImageView
import com.example.merged.R
import androidx.appcompat.app.AppCompatActivity

class FavoriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        findViewById<ImageView>(R.id.back_button_favorite)?.setOnClickListener {
            finish() // 現在のActivityを終了し、ホーム画面に戻る
        }
    }
}