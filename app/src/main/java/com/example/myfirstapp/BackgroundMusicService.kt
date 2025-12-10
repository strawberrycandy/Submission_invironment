package com.example.myfirstapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class BackgroundMusicService : Service() {
    // ★ TAGをcompanion object内に移動し、定数（const val）として定義
    companion object {
        private const val TAG = "BGMService"
    }
    private var mediaPlayer: MediaPlayer? = null

    /**
     * サービスが初めて作成されたときに呼び出されます。
     * ★ 修正点: 初期化処理を onStartCommand に移動するため、ここでは何もしません。
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate: 初期化処理を onStartCommand に委譲")
    }

    /**
     * startService()でサービスが開始されたときに呼び出されます。
     * ★ 修正点: mediaPlayer が null の場合にここで初期化・設定を行い、再生を開始します。
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: BGM再生開始を試行")

        // 【MediaPlayerのチェックと再初期化】
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bgm)

            mediaPlayer?.apply {
                isLooping = true // BGMをループ再生に設定
            }

            if (mediaPlayer == null) {
                Log.e(TAG, "MediaPlayerの初期化に失敗しました。ファイル名や配置を確認してください。")
            }
        }

        // 【再生開始】
        mediaPlayer?.run {
            // 再生中でなければ開始（既に再生中であれば何もしない）
            if (!isPlaying) {
                start()
                Log.d(TAG, "BGM再生を開始しました。")
            }
        }

        // サービスが予期せず終了しても、再作成しない設定
        return START_NOT_STICKY
    }

    /**
     * stopService()でサービスが停止されるときに呼び出されます。
     * BGMを停止し、リソースを完全に解放します。
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy: MediaPlayerのリソース解放")

        mediaPlayer?.apply {
            stop() // 再生を停止
            release() // ★ システムリソースを解放（必須）
        }
        mediaPlayer = null
    }

    /**
     * バインドされたサービスではないため、nullを返します。
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}