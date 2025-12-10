package com.example.myfirstapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R

class AnimationTestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FINAL_STAGE_INDEX = "final_stage_index"
        const val START_ANIMATION_DELAY_MS = 1000L // 1.0秒
    }

    private val nextStageImages = listOf(
        R.drawable.sakura_stage_1,
        R.drawable.sakura_stage_2,
        R.drawable.sakura_stage_3,
        R.drawable.sakura_stage_4 // 最終レベル (レベル5)
    )
    private var testStageIndex = 0 // 現在の進化回数を追跡するインデックス (0から始まる)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_test)

        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)

        // 呼び出し元から現在の進化段階を受け取る
        testStageIndex = intent.getIntExtra(EXTRA_FINAL_STAGE_INDEX, 0)

        // 🚨 初期化: ボタンを無効化し、テキストを空にする 🚨
        startEvolutionButton.isEnabled = false
        startEvolutionButton.text = ""

        // 桜の初期画像を設定 (現在のインデックスに基づいた画像)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        if (testStageIndex > 0 && testStageIndex <= nextStageImages.size) {
            sakuraImageView.setImageResource(nextStageImages[testStageIndex - 1])
        } else {
            sakuraImageView.setImageResource(R.drawable.sakura_stage_0)
        }

        // 画面が開いた直後、進化を自動で開始
        if (testStageIndex < nextStageImages.size) {
            startEvolutionButton.postDelayed({
                playSingleEvolution()
            }, START_ANIMATION_DELAY_MS)
        } else {
            // 最終レベルの場合は「戻る」ボタンを表示し、有効化
            startEvolutionButton.text = "戻る"
            startEvolutionButton.isEnabled = true
        }

        // ボタンリスナー
        startEvolutionButton.setOnClickListener {
            // 戻るボタンとして機能
            if (startEvolutionButton.isEnabled && startEvolutionButton.text == "戻る") {
                finishWithResult()
            }
        }
    }

    // 1ステップ分の進化アニメーションを再生するメソッド
    private fun playSingleEvolution() {
        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val sakuraAnimator = SakuraAnimator()

        startEvolutionButton.isEnabled = false
        startEvolutionButton.text = ""

        val nextImageId = nextStageImages[testStageIndex]

        sakuraAnimator.animateEvolution(
            whiteOverlayView,
            sakuraImageView,
            nextImageId
        ) {
            // アニメーション完了後のコールバック処理
            testStageIndex++

            // 完了したら、テキストを「戻る」にし、有効化
            startEvolutionButton.text = "戻る"
            startEvolutionButton.isEnabled = true
        }
    }

    private fun finishWithResult() {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_FINAL_STAGE_INDEX, testStageIndex)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}