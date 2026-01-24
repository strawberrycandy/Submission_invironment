package com.example.merged.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes

class SakuraAnimator {

    fun animateEvolution(
        whiteOverlayView: View,
        sakuraImageView: ImageView,
        @DrawableRes newStageImageResId: Int,
        onAnimationFinished: () -> Unit // 完了時のコールバック
    ) {

        // 🚨 修正: アニメーション時間を BGM に合わせて 3.0秒に延長 🚨
        val ANIMATION_DURATION = 3000L

        // 1. フェードアウト (ViewのALPHAを0f→1f: 透明→不透明)
        val fadeInAnimator = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, 0f, 1f).apply {
            duration = ANIMATION_DURATION
        }

        // 2. フェードイン (ViewのALPHAを1f→0f: 不透明→透明)
        val fadeOutAnimator = ObjectAnimator.ofFloat(whiteOverlayView, View.ALPHA, 1f, 0f).apply {
            duration = ANIMATION_DURATION
        }

        // --- 実行ロジック ---

        // フェードアウト完了時
        fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // 画像を次のステージに切り替える
                sakuraImageView.setImageResource(newStageImageResId)

                // フェードインアニメーションを開始
                fadeOutAnimator.start()
            }
        })

        // フェードイン完了時
        fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // コールバックを実行
                onAnimationFinished()
            }
        })

        // アニメーション開始前に、念のためアルファ値を初期化
        whiteOverlayView.alpha = 0f

        // フェードアウトアニメーションを開始
        fadeInAnimator.start()
    }
}