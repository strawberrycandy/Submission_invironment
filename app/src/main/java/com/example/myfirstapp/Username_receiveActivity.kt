package com.example.myfirstapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myfirstapp.databinding.ActivityUsernameReceiveBinding // ★★★ activity_username_receive.xml に対応

class WelcomeActivity : AppCompatActivity() {
    // View Bindingクラス名を ActivityUsernameReceiveBinding に修正
    private lateinit var binding: ActivityUsernameReceiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Bindingの初期化
        binding = ActivityUsernameReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intentからユーザー名を取得
        val username = intent.getStringExtra("EXTRA_USERNAME")

        // TextViewにユーザー名を設定
        if (username != null) {
            // TextViewのID: welcome_text_view
            binding.welcomeTextView.text = getString(R.string.welcome_message, username)
        } else {
            // ユーザー名が取得できなかった場合のフォールバック処理
            binding.welcomeTextView.text = "エラーが発生しました。"
        }
    }
}