package com.example.merged

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.first_setup.FirstStartActivity
import com.example.merged.main.Home_MainActivity // ✅ 正しいインポート
import com.example.merged.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton = findViewById<Button>(R.id.startButton)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val is_first_setting = prefs.getBoolean("is_first_setting", true)

        startButton.setOnClickListener {
            val intent = if (is_first_setting) {
                Intent(this, FirstStartActivity::class.java)
            } else {
                Intent(this, Home_MainActivity::class.java)
            }
            startActivity(intent)
        }
    }
}