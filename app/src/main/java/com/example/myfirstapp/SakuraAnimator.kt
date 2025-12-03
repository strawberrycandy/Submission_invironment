package com.example.myfirstapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes

class SakuraAnimator {

    fun animateEvolution(
        whiteOverlayView: View, // 白い覆いViewをターゲット
        sakuraImageView: ImageView,
        @DrawableRes newStageImageResId: Int
    ) {

        // 1. フェードアウト (ViewのALPHAを0f→1f: 透明→不透明)
        // XMLで whiteOverlayView.alpha は 0 に設定済みのため、0fから1fにアニメートする
        val fadeInAnimator = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, 0f, 1f).apply {
            duration = 1500L
        }

        // 2. フェードイン (ViewのALPHAを1f→0f: 不透明→透明)
        val fadeOutAnimator = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, 1f, 0f).apply {
            duration = 1500L
        }

        // --- 実行ロジック ---

        // フェードアウト完了時 (画面が完全に白くなった時) の処理
        fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 1. 画像を次のステージに切り替える（白い画面の裏側で実行）
                sakuraImageView.setImageResource(newStageImageResId)

                // 2. フェードインアニメーションを開始
                fadeOutAnimator.start()
            }
        })

        // アニメーション開始前に、念のためアルファ値を初期化
        whiteOverlayView.alpha = 0f

        // フェードアウトアニメーションを開始
        fadeInAnimator.start()
    }
}