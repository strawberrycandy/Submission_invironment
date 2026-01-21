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
        const val FADE_OUT_DURATION_MS = 500L
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

        // 現在の成長段階を計算 (0, 1, 2, 3, 4)
        val currentStage = (taskCount / 2).coerceAtMost(4)

        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val btn = findViewById<Button>(R.id.start_evolution_button)

        // BGM準備
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.evolution_bgm)
            mediaPlayer?.setVolume(INITIAL_VOLUME, INITIAL_VOLUME)
        } catch (e: Exception) { e.printStackTrace() }

        // --- 画像表示とアニメーション実行の条件分岐 ---
        when {
            // ① 偶数回（2,4,6,8回目）かつ、まだ最大(8)を超えていない場合 → 進化アニメを実行
            taskCount > 0 && taskCount % 2 == 0 && taskCount <= 8 -> {
                // 画面表示時は「進化前」の画像をセット
                val prevStage = currentStage - 1
                if (prevStage > 0) {
                    sakuraImageView.setImageResource(nextStageImages[prevStage - 1])
                } else {
                    sakuraImageView.setImageResource(R.drawable.sakura_stage_0)
                }

                // ボタンを隠して1秒後にアニメーション開始
                btn.isEnabled = false
                btn.text = ""
                btn.postDelayed({ playEvolution(currentStage) }, START_ANIMATION_DELAY_MS)
            }

            // ② それ以外（奇数回、またはすでに最大段階に達している場合） → 表示のみ
            else -> {
                if (currentStage == 0) {
                    sakuraImageView.setImageResource(R.drawable.sakura_stage_0)
                } else {
                    sakuraImageView.setImageResource(nextStageImages[currentStage - 1])
                }
                // アニメーションなし。即座に「戻る」ボタンを表示
                btn.text = "戻る"
                btn.isEnabled = true
            }
        }

        btn.setOnClickListener {
            startActivity(Intent(this, Home_MainActivity::class.java))
            finish()
        }
    }

    private fun playEvolution(targetStageIndex: Int) {
        mediaPlayer?.start()
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        val btn = findViewById<Button>(R.id.start_evolution_button)

        val safeIndex = targetStageIndex.coerceIn(1, 4)
        SakuraAnimator().animateEvolution(whiteOverlayView, sakuraImageView, nextStageImages[safeIndex - 1]) {
            fadeOutMusic {
                // 結果をセット（必要であれば）
                val res = Intent().apply { putExtra(EXTRA_FINAL_STAGE_INDEX, targetStageIndex) }
                setResult(RESULT_OK, res)

                btn.text = "戻る"
                btn.isEnabled = true
            }
        }
    }

    private fun fadeOutMusic(onFinished: () -> Unit) {
        val animator = ValueAnimator.ofFloat(INITIAL_VOLUME, 0f).apply { duration = FADE_OUT_DURATION_MS }
        animator.addUpdateListener { animation ->
            val v = animation.animatedValue as Float
            mediaPlayer?.setVolume(v, v)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                mediaPlayer?.pause()
                onFinished()
            }
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}