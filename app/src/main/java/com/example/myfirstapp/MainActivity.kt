package com.example.myfirstapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import android.content.Intent // Intentをインポート

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 過去の通知権限チェック、createNotificationChannel()呼び出しは削除

        // ボタン取得
        val startButton = findViewById<Button>(R.id.startButton)
        if (startButton == null) {
            println("startButton が null です！ID または setContentView を確認してください")
            Toast.makeText(this, "ボタンが見つかりません！", Toast.LENGTH_LONG).show()
            return
        }

        startButton.setOnClickListener {
            println("startButtonが押されました")
            Toast.makeText(this, "スタート", Toast.LENGTH_SHORT).show()

            // TODO: ここに次の画面への遷移ロジックを記述します。
            // val intent = Intent(this, NextActivity::class.java)
            // startActivity(intent)
        }
    }

}
