package com.example.merged.first_setup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences から username を取得
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val username = prefs.getString("username", "ゲスト")

        // 表示
        binding.textWelcome.text = "${username} さん\nようこそ！"


        // 3秒後にフェードアウトして画面遷移
        Handler(Looper.getMainLooper()).postDelayed({
            startFadeOutAndNavigate()
        }, 3000)
    }

    private fun startFadeOutAndNavigate() {
        // フェードアウトアニメーション
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 800 // フェード時間（0.8秒）
            fillAfter = true
        }

        binding.textWelcome.startAnimation(fadeOut)

        // フェード完了後に画面遷移
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Tutorial_MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 800)
    }
}
