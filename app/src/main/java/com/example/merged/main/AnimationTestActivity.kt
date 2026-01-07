package com.example.merged.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.merged.R
import com.example.merged.main.SakuraAnimator

class AnimationTestActivity : AppCompatActivity() {

    private val stageImages = listOf(
        R.drawable.sakura_stage_0, R.drawable.sakura_stage_1,
        R.drawable.sakura_stage_2, R.drawable.sakura_stage_3,
        R.drawable.sakura_stage_4
    )

    private var mediaPlayer: MediaPlayer? = null

    // 計算結果を保持する変数
    private var oldStage: Int = 0
    private var newStage: Int = 0
    private var newTotalTasks: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_test)

        // --- ロジックの再構築 ---
        // 1. 現在の状態をSharedPreferencesから読み込む
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val currentTotalTasks = prefs.getInt("tasksWithThisCherryBlossom", 0)
        oldStage = prefs.getInt("cherryBlossomGrowthStage", 0)

        // 2. 新しい状態を計算する
        newTotalTasks = currentTotalTasks + 1
        newStage = (newTotalTasks / 2).coerceIn(0, 4)

        // 3. 最初の表示をセット (進化前の木)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        sakuraImageView.setImageResource(stageImages.getOrElse(oldStage) { R.drawable.sakura_stage_0 })

        mediaPlayer = MediaPlayer.create(this, R.raw.evolution_bgm)
        mediaPlayer?.setVolume(0.5f, 0.5f)

        // 4. 成長段階が上がったかチェック
        val hasEvolved = newStage > oldStage

        // 1秒後にアニメーション開始または終了処理
        sakuraImageView.postDelayed({
            if (hasEvolved) {
                playEvolution()
            } else {
                // 進化しない場合は、データだけ更新してすぐに戻る
                finalizeAndReturn()
            }
        }, 1000L)
    }

    private fun playEvolution() {
        mediaPlayer?.start()
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)

        // 次のステージの画像をセットしてアニメーション
        val nextImageId = stageImages.getOrElse(newStage) { R.drawable.sakura_stage_4 }
        SakuraAnimator().animateEvolution(whiteOverlayView, sakuraImageView, nextImageId) {
            fadeOutMusic { finalizeAndReturn() }
        }
    }

    // データを保存してホームへ戻る処理
    private fun finalizeAndReturn() {
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        whiteOverlayView.visibility = View.VISIBLE

        val fadeIn = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, whiteOverlayView.alpha, 1.0f)
        fadeIn.duration = 2000L
        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 計算済みの新しい状態でSharedPreferencesを更新
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putInt("tasksWithThisCherryBlossom", newTotalTasks)
                editor.putInt("cherryBlossomGrowthStage", newStage)
                editor.apply()

                // 全て完了してホームに戻る
                val intent = Intent(this@AnimationTestActivity, Home_MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        })
        fadeIn.start()
    }

    private fun fadeOutMusic(onFinished: () -> Unit) {
        val animator = ValueAnimator.ofFloat(0.5f, 0f).apply { duration = 500L }
        animator.addUpdateListener { mediaPlayer?.setVolume(it.animatedValue as Float, it.animatedValue as Float) }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mediaPlayer?.pause()
                onFinished()
            }
        })
        animator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}