package com.example.myfirstapp

import android.content.Intent // 🚨 この行を追記してください 🚨
import android.os.Bundle
import android.widget.Button // 🚨 この行を追記してください 🚨
import androidx.appcompat.app.AppCompatActivity

class SetupV1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_v1)

        // XMLのID: start_button を使用 (activity_setup_v1.xmlから確認)
        val startButton = findViewById<Button>(R.id.start_button)

        // 🚨 ここから遷移ロジックを追記 🚨
        startButton.setOnClickListener {
            // 次の画面（SetupV2TermsActivity: 利用規約画面）へ遷移
            val intent = Intent(this, SetupV2TermsActivity::class.java)
            startActivity(intent)
        }
        // 🚨 追記ここまで 🚨
    }
}