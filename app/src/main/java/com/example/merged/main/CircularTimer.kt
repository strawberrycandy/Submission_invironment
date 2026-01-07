package com.example.merged.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularTimer(progress: Float, timeLeft: Int) {

    val sweepAngle = 360 * progress

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF55F0AE), Color(0xFF1FA85C))
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            // グレーの背景円
            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true
            )

            // 緑色グラデーションの円（残り時間）
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = true
            )
        }

        // --- 数字の色切り替え ---
        val numberColor = Color.White

        Text(
            text = timeLeft.toString(),
            fontSize = 70.sp,
            fontWeight = FontWeight.Bold,
            color = numberColor
        )
    }
}
