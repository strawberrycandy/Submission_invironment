package com.example.merged.main

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R

class AnimationTestActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_FINAL_STAGE_INDEX = "final_stage_index"
        const val START_ANIMATION_DELAY_MS = 1000L
        const val INITIAL_VOLUME = 0.5f
    }

    private val nextStageImages = listOf(
        R.drawable.sakura_stage_1, R.drawable.sakura_stage_2,
        R.drawable.sakura_stage_3, R.drawable.sakura_stage_4
    )
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_test)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val taskCount = prefs.getInt("tasksWithThisCherryBlossom", 0)

        // ロジック：2回ごとに1段階アップ
        val beforeCount = if (taskCount > 0) taskCount - 1 else 0
        val startStage = (beforeCount / 2).coerceAtMost(4)
        val endStage = (taskCount / 2).coerceAtMost(4)

        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val btn = findViewById<Button>(R.id.start_evolution_button)

        // 初期画像セット
        if (startStage > 0) sakuraImageView.setImageResource(nextStageImages[startStage - 1])
        else sakuraImageView.setImageResource(R.drawable.sakura_stage_0)

        // 偶数回（2,4,6,8）なら進化アニメ、奇数回ならそのまま
        if (taskCount > 0 && taskCount % 2 == 0) {
            btn.postDelayed({ playEvolution(endStage) }, START_ANIMATION_DELAY_MS)
        } else {
            btn.text = "戻る"
            btn.isEnabled = true
        }

        btn.setOnClickListener {
            startActivity(Intent(this, Home_MainActivity::class.java))
            finish()
        }
    }

    private fun playEvolution(targetStageIndex: Int) {
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val btn = findViewById<Button>(R.id.start_evolution_button)

        val safeIndex = targetStageIndex.coerceIn(1, 4)
        SakuraAnimator().animateEvolution(whiteOverlayView, sakuraImageView, nextStageImages[safeIndex - 1]) {
            // Evo_MainActivityへの報告
            val res = Intent().apply { putExtra(EXTRA_FINAL_STAGE_INDEX, targetStageIndex) }
            setResult(RESULT_OK, res)
            btn.text = "戻る"
            btn.isEnabled = true
        }
    }
}