package com.example.myfirstapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BreakTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_task)

        val backButton = findViewById<Button>(R.id.back_to_main_button)
        backButton.setOnClickListener {
            // このActivityを終了し、スタックの最上位にあるMainActivityに戻る
            finish()
        }
    }
}