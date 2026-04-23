package com.example.merged.first_setup

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.merged.R

/**
 * 各ページでフルスクリーン表示する画像
 */
enum class TutorialStage(val imageResId: Int) {
    TUTORIAL(R.drawable.tutorial),
    ME(R.drawable.me2),
    TREE(R.drawable.tree2)
}

/**
 * ページ定義（6ページ）
 */
data class TutorialPage(
    val id: Int,
    val text: String,
    val stage: TutorialStage
)

val tutorialPages = listOf(
    TutorialPage(1, "ここは (ユーザー名) さんの庭です。", TutorialStage.TUTORIAL),
    TutorialPage(2, "まずは桜の木を植えてみましょう...", TutorialStage.TUTORIAL),
    TutorialPage(3, "", TutorialStage.ME),
    TutorialPage(4, "この桜は、あなたが目を休めるたびに成長します。", TutorialStage.TREE),
    TutorialPage(5, "逆に、目を休めないと桜はどんどん悪くなっていきます。", TutorialStage.ME),
    TutorialPage(6, "では、さっそく育ててみましょう！", TutorialStage.TREE)
)

/**
 * チュートリアル画面
 */
@Composable
fun TutorialScreen(
    currentPage: Int,
    onNextPage: () -> Unit,
    userName: String,
    isFading: Boolean
) {
    val page = tutorialPages.first { it.id == currentPage }

    val alpha by animateFloatAsState(
        targetValue = if (isFading) 0f else 1f,
        animationSpec = tween(durationMillis = 3000),
        label = "FadeOut"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {

        // 背景画像
        Crossfade(
            targetState = page.stage,
            animationSpec = tween(800),
            label = "ImageFade"
        ) { stage ->
            Image(
                painter = painterResource(stage.imageResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // テキスト & ボタン
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            Crossfade(
                targetState = page.text,
                animationSpec = tween(800),
                label = "TextFade"
            ) { t ->
                Text(
                    text = t.replace("(ユーザー名)", userName),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            Button(onClick = onNextPage, enabled = !isFading) {
                Text(if (currentPage < tutorialPages.size) "次へ" else "はじめる")
            }
        }
    }
}
