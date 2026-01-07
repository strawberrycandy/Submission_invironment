package com.example.merged.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.merged.R

class Evo_MainActivity : AppCompatActivity() {

    // ローカルでのテスト表示用のインデックス
    private var currentSakuraStageIndex: Int = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                println("通知許可が与えられました")
            } else {
                println("通知許可が拒否されました")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注意: このレイアウトは他のActivityと共有されている可能性があります
        setContentView(R.layout.activity_start)

        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)

        updateEvolutionButtonText()

        startEvolutionButton.setOnClickListener {
            // AnimationTestActivity を単純に起動する
            startActivity(Intent(this, AnimationTestActivity::class.java))

            // テストUIのため、ローカルのインデックスを更新してボタン表示を変える
            if (currentSakuraStageIndex < 4) {
                currentSakuraStageIndex++
                updateEvolutionButtonText()
            }
        }

        // 通知権限チェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        createNotificationChannel()
    }

    // 進化ボタンのテキストを更新するメソッド
    private fun updateEvolutionButtonText() {
        val startEvolutionButton = findViewById<Button>(R.id.start_evolution_button)
        // 桜のステージは全部で5段階 (レベル0からレベル4まで)と仮定
        if (currentSakuraStageIndex < 4) {
            // 次に進化するレベルをテキストに表示 (インデックス+1 = レベル)
            startEvolutionButton.text = "進化アニメーション再生 (レベル${currentSakuraStageIndex + 1}へ)"
            startEvolutionButton.isEnabled = true
        } else {
            startEvolutionButton.text = "最終レベルに到達しました"
            startEvolutionButton.isEnabled = false // 最終レベル到達後は無効化
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "eye_rest_channel",
                "休憩通知チャンネル",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}