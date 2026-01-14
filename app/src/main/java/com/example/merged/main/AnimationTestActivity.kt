package com.example.merged.main

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.ObjectAnimator // 🚨 デバッグ用フェードインで使用 🚨
import com.example.merged.R
import androidx.compose.ui.platform.LocalContext

class AnimationTestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FINAL_STAGE_INDEX = "final_stage_index"
        const val START_ANIMATION_DELAY_MS = 1000L // 1.0秒

        // BGM/音量調整用
        const val FADE_OUT_DURATION_MS = 500L // BGMのフェードアウト時間 (0.5秒)
        const val INITIAL_VOLUME = 0.5f

        // デバッグ画面のフェードイン時間
        const val FADE_IN_DURATION_DEBUG = 2000L // 白い覆いが不透明になるまでの時間 (2.0秒)
    }

    private val nextStageImages = listOf(
        R.drawable.sakura_stage_1,
        R.drawable.sakura_stage_2,
        R.drawable.sakura_stage_3,
        R.drawable.sakura_stage_4 // 最終レベル (レベル5)
    )

    private var testStageIndex = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation_test)

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.evolution_bgm)
            mediaPlayer?.isLooping = false
            mediaPlayer?.setVolume(INITIAL_VOLUME, INITIAL_VOLUME)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)
        // MainActivityから現在のステージインデックスを受け取る
        testStageIndex = intent.getIntExtra(EXTRA_FINAL_STAGE_INDEX, 0)

        startEvolutionButton.isEnabled = false
        startEvolutionButton.text = ""

        val sakuraImageView = findViewById<ImageView>(R.id.main_sakura_image_view)
        if (testStageIndex > 0 && testStageIndex <= nextStageImages.size) {
            sakuraImageView.setImageResource(nextStageImages[testStageIndex - 1])
        } else {
            sakuraImageView.setImageResource(R.drawable.sakura_stage_0)
        }

        // 1秒遅延後、進化を自動で開始
        if (testStageIndex < nextStageImages.size) {
            startEvolutionButton.postDelayed({
                playSingleEvolution()
            }, START_ANIMATION_DELAY_MS)
        } else {
            startEvolutionButton.text = "戻る"
            startEvolutionButton.isEnabled = true
        }

        startEvolutionButton.setOnClickListener {
            if (startEvolutionButton.isEnabled && startEvolutionButton.text == "戻る") {
                // 🚨 画面遷移をせず、デバッグ表示に切り替える 🚨
                showDebugMessageAndStop()
            }
        }
    }

    // ライフサイクル管理：Activity破棄時にMediaPlayerを解放
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // 1ステップ分の進化アニメーションを再生するメソッド
    private fun playSingleEvolution() {
        // BGM再生開始
        mediaPlayer?.start()

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

            // BGMフェードアウト
            fadeOutMusic {
                // BGMのフェードアウトが完全に終わった後、残りの処理を行う
                testStageIndex++

                startEvolutionButton.text = "戻る"
                startEvolutionButton.isEnabled = true
            }
        }
    }


    // 🚨 画面遷移を無効化し、デバッグメッセージをフェードイン表示するメソッド 🚨
    private fun showDebugMessageAndStop() {

        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)
        val whiteOverlayView = findViewById<View>(R.id.whiteOverlayView)
        val debugMessageText = findViewById<TextView>(R.id.debug_message_text)

        // 1. ボタンを無効化
        startEvolutionButton.isEnabled = false
        startEvolutionButton.text = ""

        // 2. 白い覆いの visibility を確実にVISIBLEにする
        whiteOverlayView.visibility = View.VISIBLE

        // 3. 白い覆いのフェードインアニメーターを定義
        val fadeInOverlayAnimator = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, whiteOverlayView.alpha, 1.0f).apply {
            duration = FADE_IN_DURATION_DEBUG
        }

        // 4. BGMをフェードアウト
        if (mediaPlayer?.isPlaying == true) {
            fadeOutMusic {
                // BGM停止後、白い覆いのフェードインを開始
                fadeInOverlayAnimator.start()
            }
        } else {
            // BGMが再生されていなければ、すぐに白い覆いのフェードインを開始
            fadeInOverlayAnimator.start()
        }

        // 5. フェードイン完了時の処理
        fadeInOverlayAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

                prefs.edit().putInt("tasksWithThisCherryBlossom",
                    prefs.getInt(("tasksWithThisCherryBlossom"), 0) + 1).apply()
                prefs.edit().putInt("taskCountTotal",
                    prefs.getInt(("taskCountTotal"), 0) + 1).apply()

                val intent = Intent(this@AnimationTestActivity, Home_MainActivity::class.java)
                startActivity(intent)
                finish() // Home_MainActivity に戻る
            }

        })
    }

    /**
     * BGMの音量を徐々に下げて停止させる処理
     */
    private fun fadeOutMusic(onFinished: () -> Unit) {
        val animator = ValueAnimator.ofFloat(INITIAL_VOLUME, 0f).apply {
            duration = FADE_OUT_DURATION_MS
        }

        animator.addUpdateListener { animation ->
            val volume = animation.animatedValue as Float
            // アニメーションに合わせて音量をリアルタイムで更新
            mediaPlayer?.setVolume(volume, volume)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // 音量が0になったら完全に停止し、頭出しする
                mediaPlayer?.pause()
                mediaPlayer?.seekTo(0)
                // 完了コールバックを実行
                onFinished()
            }
        })

        animator.start()
    }
}