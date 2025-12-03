package com.example.myfirstapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

// Rクラスへの参照を確実にするため、明示的にインポートを追加することが推奨されます
import com.example.myfirstapp.R

class AnimationTestActivity : AppCompatActivity() {

    // 桜が進化する次のステージの画像IDを順に保持する（テスト用）
    private val nextStageImages = listOf(
        R.drawable.sakura_stage_1,
        R.drawable.sakura_stage_2,
        R.drawable.sakura_stage_3,
        R.drawable.sakura_stage_4
    )
    private var testStageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_test)

        // 白い覆い用のViewと桜のImageView、ボタンを取得
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView) // 🚨 修正: whiteOverlayView を取得 🚨
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)

        val sakuraAnimator = SakuraAnimator()

        startEvolutionButton.setOnClickListener {
            if (testStageIndex < nextStageImages.size) {
                // アニメーションを実行
                sakuraAnimator.animateEvolution(
                    whiteOverlayView,
                    sakuraImageView,
                    nextStageImages[testStageIndex]
                )
                // テスト用のステージインデックスを更新
                testStageIndex++
            } else {
                // すべてのステージが完了したら、ボタンのテキストを変更
                startEvolutionButton.text = "進化完了（リセットが必要）"
            }
        }
    }
}