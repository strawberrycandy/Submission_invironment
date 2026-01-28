package com.example.merged.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_result) // レイアウトファイルが存在しない可能性を考慮しコメントアウト
        setContentView(R.layout.activity_tasks_status)
    }
}