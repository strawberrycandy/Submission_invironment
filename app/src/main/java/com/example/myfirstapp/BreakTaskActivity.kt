package com.example.myfirstapp

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BreakTaskActivity : AppCompatActivity() {

    // レイアウト要素の lateinit プロパティを宣言
    private lateinit var breakTimerValue: TextView
    private lateinit var breakStartButton: Button

    // 30秒のタイマーインスタンス
    private lateinit var countDownTimer: CountDownTimer

    // タイマーの初期値 (30秒 = 30000ミリ秒)
    private val START_TIME_MILLIS: Long = 30000
    // 現在の残り時間
    private var timeLeftInMillis = START_TIME_MILLIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_task)

        // 1. レイアウト要素の紐づけ
        breakTimerValue = findViewById(R.id.breakTimerValue)
        breakStartButton = findViewById(R.id.breakStartButton)

        // 2. CountDownTimerの初期化
        countDownTimer = object : CountDownTimer(START_TIME_MILLIS, 1000) {

            // 1秒ごとに呼ばれる処理
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                // ミリ秒を秒に変換して表示を更新
                val seconds = (timeLeftInMillis / 1000).toInt()
                breakTimerValue.text = seconds.toString()
            }

            // タイマーが完了したときの処理
            override fun onFinish() {
                breakTimerValue.text = "0"
                breakStartButton.text = "休憩完了！"
                breakStartButton.isEnabled = true // 完了後は再度ボタンを押せるようにする
                Toast.makeText(this@BreakTaskActivity, "休憩時間終了です！", Toast.LENGTH_LONG).show()
                // ここでMainActivityに戻る処理などを追加できます
            }
        }

        // 3. ボタンクリックリスナーの設定
        breakStartButton.setOnClickListener {
            startTimer()
            // タイマー開始後、ボタンを無効化し、テキストを変更する
            breakStartButton.text = "タイマー実行中..."
            breakStartButton.isEnabled = false
        }

        // 初回起動時にタイマーの初期値（30）を表示
        updateCountdownText()
    }

    private fun startTimer() {
        countDownTimer.start()
    }

    private fun updateCountdownText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        breakTimerValue.text = seconds.toString()
    }

    /**
     * Activityが破棄される際にタイマーを停止し、メモリリークを防ぐ
     */
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}