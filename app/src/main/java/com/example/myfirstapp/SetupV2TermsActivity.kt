package com.example.myfirstapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox // 🚨 追加
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetupV2TermsActivity : AppCompatActivity() {

    // スクロール完了フラグをクラス内で保持
    private var isScrolledToBottom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_v2_terms)

        val scrollView = findViewById<ScrollView>(R.id.terms_scroll_view)
        val agreementCheckbox = findViewById<CheckBox>(R.id.agreement_checkbox) // 🚨 チェックボックスを取得
        val nextButton = findViewById<Button>(R.id.next_button) // 🚨 次へボタンを取得

        if (scrollView == null || agreementCheckbox == null || nextButton == null) return

        // --- 1. 初期状態の設定 ---

        // チェックボックスの初期状態を無効にする
        agreementCheckbox.isEnabled = false
        // 「次へ」ボタンの初期状態を無効にする（チェックボックスが押されていないため）
        nextButton.isEnabled = false

        // --- 2. スクロール監視ロジック ---

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (isScrolledToBottom) return@addOnScrollChangedListener // 既に到達済みなら何もしない

            // スクロールビューの最大のスクロール量 (子ビューの高さ - スクロールビュー自体の高さ)
            val maxScroll = scrollView.getChildAt(0).height - scrollView.height

            // スクロールが最後まで到達したか判定 (50pxのマージンを設けて判定)
            if (scrollView.scrollY >= maxScroll - 50) {
                isScrolledToBottom = true

                // スクロール完了時にチェックボックスを有効化する
                agreementCheckbox.isEnabled = true
                Toast.makeText(this, "規約全文を確認しました。チェックボックスが有効になりました。", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 3. チェックボックスの状態監視ロジック ---

        agreementCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // チェックボックスの状態に応じて「次へ」ボタンの有効/無効を切り替える
            nextButton.isEnabled = isChecked
        }

        // --- 4. 画面遷移ロジック ---

        nextButton.setOnClickListener {
            if (agreementCheckbox.isChecked) {
                // チェック済みなら次の画面（SetupV3UserNameActivity）へ遷移
                val intent = Intent(this, SetupV3UserNameActivity::class.java)
                startActivity(intent)
            } else {
                // 通常はボタンが無効なのでここには来ないが、念のため
                Toast.makeText(this, "利用規約への同意が必要です。", Toast.LENGTH_SHORT).show()
            }
        }
    }
}