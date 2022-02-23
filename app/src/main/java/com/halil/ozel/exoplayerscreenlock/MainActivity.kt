package com.halil.ozel.exoplayerscreenlock

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.halil.ozel.exoplayerscreenlock.databinding.ActivityMainBinding
import com.halil.ozel.exoplayerscreenlock.databinding.CustomControllerBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val URL =
            "http://185.184.208.112/contents/7715ADCD-3948-4973-9561-580D2B72BA75/HLS/IOS-MOB-HLS-FP/master.m3u8"
        var isFullScreen = false
        var isLock = false
    }

    private var playbackPosition = 0L
    private var playWhenReady = true

    private lateinit var binding: ActivityMainBinding
    private lateinit var customControllerBinding: CustomControllerBinding
    private var exoPlayer: ExoPlayer? = null
    lateinit var bt_fullscreen: ImageView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        preparePlayer()
        bt_fullscreen =  findViewById(R.id.bt_fullscreen)
        val bt_lockscreen = findViewById<ImageView>(R.id.exo_lock)

        //customControllerBinding = CustomControllerBinding.inflate(layoutInflater,binding.player,true)


        bt_fullscreen.setOnClickListener {

            if (!isFullScreen) {
                bt_fullscreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen_exit
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                bt_fullscreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            isFullScreen = !isFullScreen
        }

        bt_lockscreen.setOnClickListener {
            if (!isLock) {
                bt_lockscreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_lock
                    )
                )
            } else {
                bt_lockscreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_lock_open
                    )
                )
            }
            isLock = !isLock
            lockScreen(isLock)
        }
    }

    private fun preparePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer?.playWhenReady = true
        binding.player.player = exoPlayer
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem =
            MediaItem.fromUri(URL)
        val mediaSource =
            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.seekTo(playbackPosition)
        exoPlayer?.playWhenReady = playWhenReady
        exoPlayer?.prepare()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.release()
            exoPlayer = null
        }
    }

    private fun lockScreen(lock: Boolean) {
        val sec_mid = findViewById<LinearLayout>(R.id.sec_controlvid1)
        val sec_bottom = findViewById<LinearLayout>(R.id.sec_controlvid2)
        if (lock) {
            sec_mid.visibility = View.INVISIBLE
            sec_bottom.visibility = View.INVISIBLE
        } else {
            sec_mid.visibility = View.VISIBLE
            sec_bottom.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bt_fullscreen.performClick()
        } else super.onBackPressed()

    }


    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }
}