package com.example.myfirstapp

import android.content.Intent
import android.os.Bundle // ★★★ Bundleをインポート
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity // ★★★ AppCompatActivityをインポート
import androidx.core.content.ContextCompat
import com.example.myfirstapp.databinding.ActivityUserNameBinding // ★★★ ViewBindingクラスをインポート

class UsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserNameBinding
    private val minLength = 2
    private val maxLength = 8

    // onCreateのオーバーライドに必要なBundleのインポートが完了
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root) // エラー解消 (binding.root は View です)

        // 初期状態で登録完了ボタンを無効にする
        binding.buttonRegister.isEnabled = false

        // EditTextの入力監視 (IDはXMLと一致)
        binding.editUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkInputLength(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 登録完了ボタンのクリックリスナー (IDはXMLと一致)
        binding.buttonRegister.setOnClickListener {
            val username = binding.editUsername.text.toString()
            if (binding.buttonRegister.isEnabled) {
                navigateToWelcomeScreen(username)
            }
        }
    }

    private fun checkInputLength(text: String) {
        val length = text.length
        val isValid = length >= minLength && length <= maxLength

        if (isValid) {
            binding.textLengthCheck.text = "✅ 2〜8文字" // IDはXMLと一致
            binding.textLengthCheck.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            binding.textLengthCheck.text = "2〜8文字" // IDはXMLと一致
            binding.textLengthCheck.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }

        binding.buttonRegister.isEnabled = isValid // IDはXMLと一致
    }

    private fun navigateToWelcomeScreen(username: String) {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            putExtra("EXTRA_USERNAME", username)
        }
        startActivity(intent)
    }
}