package com.example.merged.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class Evo_MainActivity : AppCompatActivity() {

    private val evolutionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                // AnimationTestActivityから送られてくる結果を受け取る
                val finalIndex = data?.getIntExtra(AnimationTestActivity.EXTRA_FINAL_STAGE_INDEX, 0) ?: 0

                Toast.makeText(this, "桜がレベル${finalIndex + 1}に進化しました！", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ここにレイアウト設定やボタンのクリックリスナーがあるはずです
    }
}