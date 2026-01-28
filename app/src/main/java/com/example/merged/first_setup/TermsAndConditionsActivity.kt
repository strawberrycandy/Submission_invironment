package com.example.merged.first_setup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ScrollView // ScrollViewをインポート
import android.widget.Toast
import android.content.Intent
import com.example.merged.R

class TermsAndConditionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val scrollView = findViewById<ScrollView>(R.id.terms_scroll_view) // ScrollViewのIDを取得
        val agreeButton = findViewById<Button>(R.id.agree_button)
        val disagreeButton = findViewById<Button>(R.id.disagree_button)

        // 🔴 1. 初期状態で同意ボタンを無効にする 🔴
        agreeButton.isEnabled = false

        // 🔴 2. スクロール監視リスナーの設定 🔴
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            // スクロール可能な最大値を取得
            val maxScroll = scrollView.getChildAt(0).height - scrollView.height

            // 現在のスクロール位置が最大値に近いかどうかをチェック (許容誤差を設ける)
            if (scrollView.scrollY >= maxScroll - 50) {
                // スクロールが最下部に到達
                if (!agreeButton.isEnabled) {
                    agreeButton.isEnabled = true
                }
            }
        }

        // 「同意する」ボタンの処理 (変更なし)
        agreeButton.setOnClickListener {
            val intent = Intent(this, UsernameActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 「同意しない」ボタンの処理 (変更なし)
        disagreeButton.setOnClickListener {
            Toast.makeText(this, "利用規約に同意しませんでした。アプリを終了します。", Toast.LENGTH_LONG).show()
            finishAffinity()
        }
    }
}