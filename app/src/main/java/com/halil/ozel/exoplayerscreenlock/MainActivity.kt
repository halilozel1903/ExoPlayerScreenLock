package com.halil.ozel.exoplayerscreenlock

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.halil.ozel.exoplayerscreenlock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var playbackPosition = 0L
    private var playWhenReady = true

    private lateinit var binding: ActivityMainBinding
    private var exoPlayer: ExoPlayer? = null
    private lateinit var imageViewFullScreen: ImageView
    private lateinit var imageViewLock: ImageView
    private lateinit var linearLayoutControlUp: LinearLayout
    private lateinit var linearLayoutControlBottom: LinearLayout

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        preparePlayer()
        imageViewFullScreen = findViewById(R.id.imageViewFullScreen)
        imageViewLock = findViewById(R.id.imageViewLock)
        linearLayoutControlUp = findViewById(R.id.linearLayoutControlUp)
        linearLayoutControlBottom = findViewById(R.id.linearLayoutControlBottom)

        setLockScreen()
        setFullScreen()

    }

    private fun preparePlayer() {
        exoPlayer = ExoPlayer.Builder(this).setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
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


    private fun lockScreen(lock: Boolean) {
        if (lock) {
            linearLayoutControlUp.visibility = View.INVISIBLE
            linearLayoutControlBottom.visibility = View.INVISIBLE
        } else {
            linearLayoutControlUp.visibility = View.VISIBLE
            linearLayoutControlBottom.visibility = View.VISIBLE
        }
    }

    private fun setLockScreen() {
        imageViewLock.setOnClickListener {
            if (!isLock) {
                imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_lock
                    )
                )
            } else {
                imageViewLock.setImageDrawable(
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

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullScreen() {
        imageViewFullScreen.setOnClickListener {

            if (!isFullScreen) {
                imageViewFullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen_exit
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                imageViewFullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            isFullScreen = !isFullScreen
        }
    }

    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageViewFullScreen.performClick()
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

    companion object {
        const val URL =
            "http://185.184.208.112/contents/7715ADCD-3948-4973-9561-580D2B72BA75/HLS/IOS-MOB-HLS-FP/master.m3u8"
        var isFullScreen = false
        var isLock = false
    }
}