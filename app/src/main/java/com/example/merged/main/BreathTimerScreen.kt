package com.example.merged.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.merged.first_setup.Tutorial_MainActivity

@Composable
fun BreathTimerScreen(onTaskFinished: () -> Unit) {

    val context = LocalContext.current

    var taskList = listOf(
        "深呼吸をしましょう...！",
        "遠くの景色を眺めましょう...！",
        "部屋を歩き回ってみましょう...！"
    )

    var isStarted by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(30) }
    var showFinished by remember { mutableStateOf(false) }
    var fadeOut by remember { mutableStateOf(false) }
    var currentTask by remember { mutableStateOf(taskList.random()) }

    // ふわふわ上下アニメ
    val floatAnim by rememberInfiniteTransition().animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // スタートで下がる
    val offsetY by animateDpAsState(
        targetValue = if (isStarted) 40.dp else 0.dp,
        animationSpec = tween(700)
    )

    // フェードアウト
    val fadeAlpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(800)
    )

    // カウントダウンのコルーチン
    LaunchedEffect(isStarted) {
        if (isStarted) {
            for (t in 30 downTo 0) {
                timeLeft = t
                delay(1000L)
            }

            showFinished = true
            delay(3000L)
            fadeOut = true
            onTaskFinished()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(fadeAlpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // 上の説明文
            if (!isStarted) {
                Text(
                    text = currentTask,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // タイマー（ふわふわ + 下がる）
            Box(modifier = Modifier.offset(y = offsetY + floatAnim.dp)) {
                CircularTimer(progress = timeLeft / 30f, timeLeft = timeLeft)
            }

            Spacer(modifier = Modifier.height(100.dp))

            // 下のメッセージ
            Text(
                text = when {
                    !isStarted -> ""
                    showFinished -> "タスクが完了しました！\nお疲れ様でした"
                    else -> "タスクを実行中..."
                },
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (!isStarted) {
                Button(
                    onClick = { isStarted = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(text = "スタート", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
