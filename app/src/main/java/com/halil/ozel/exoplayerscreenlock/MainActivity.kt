package com.halil.ozel.exoplayerscreenlock

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.halil.ozel.exoplayerscreenlock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null

    private lateinit var fullScreenButton: ImageView
    private lateinit var lockButton: ImageView
    private lateinit var topControls: LinearLayout
    private lateinit var bottomControls: LinearLayout

    private var playbackPosition = 0L
    private var playWhenReady = true
    private var isFullScreen = false
    private var isLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restorePlayerState(savedInstanceState)
        configureEdgeToEdgePlayer()
        setView()
        bindControllerViews()
        setupControls()
        setupBackNavigation()
        initializePlayer()
    }

    override fun onStart() {
        super.onStart()
        if (player == null) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) initializePlayer()
        updateSystemBars()
        player?.playWhenReady = playWhenReady
    }

    override fun onPause() {
        savePlaybackState()
        player?.pause()
        super.onPause()
    }

    override fun onStop() {
        savePlaybackState()
        releasePlayer()
        super.onStop()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        savePlaybackState()
        outState.putLong(KEY_PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
        outState.putBoolean(KEY_IS_FULLSCREEN, isFullScreen)
        outState.putBoolean(KEY_IS_LOCKED, isLocked)
        super.onSaveInstanceState(outState)
    }

    private fun setView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun configureEdgeToEdgePlayer() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun bindControllerViews() {
        fullScreenButton = findViewById(R.id.imageViewFullScreen)
        lockButton = findViewById(R.id.imageViewLock)
        topControls = findViewById(R.id.linearLayoutControlUp)
        bottomControls = findViewById(R.id.linearLayoutControlBottom)
    }

    private fun setupControls() {
        updateLockState()
        updateFullScreenState()

        lockButton.setOnClickListener {
            isLocked = !isLocked
            updateLockState()
        }

        fullScreenButton.setOnClickListener {
            toggleFullScreen()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        isLocked -> Unit
                        isFullScreen || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ->
                            exitFullScreen()
                        else -> {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        )
    }

    private fun restorePlayerState(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        playbackPosition = savedInstanceState.getLong(KEY_PLAYBACK_POSITION, 0L)
        playWhenReady = savedInstanceState.getBoolean(KEY_PLAY_WHEN_READY, true)
        isFullScreen = savedInstanceState.getBoolean(KEY_IS_FULLSCREEN, false)
        isLocked = savedInstanceState.getBoolean(KEY_IS_LOCKED, false)
    }

    private fun initializePlayer() {
        if (player != null) return

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MILLIS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MILLIS)
            .build()
            .also { exoPlayer ->
                binding.player.player = exoPlayer
                exoPlayer.setMediaItem(MediaItem.fromUri(STREAM_URL))
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    private fun savePlaybackState() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            playWhenReady = exoPlayer.playWhenReady
        }
    }

    private fun releasePlayer() {
        savePlaybackState()
        binding.player.player = null
        player?.release()
        player = null
    }

    private fun updateLockState() {
        val controlsVisibility = if (isLocked) View.INVISIBLE else View.VISIBLE
        topControls.visibility = controlsVisibility
        bottomControls.visibility = controlsVisibility
        lockButton.setImageResource(
            if (isLocked) R.drawable.ic_baseline_lock else R.drawable.ic_baseline_lock_open
        )
        lockButton.contentDescription = getString(
            if (isLocked) R.string.unlock_controls else R.string.lock_controls
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun toggleFullScreen() {
        if (isFullScreen) {
            exitFullScreen()
        } else {
            isFullScreen = true
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            updateFullScreenState()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun exitFullScreen() {
        isFullScreen = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        updateFullScreenState()
    }

    private fun updateFullScreenState() {
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                if (isFullScreen) R.drawable.ic_baseline_fullscreen_exit else R.drawable.ic_baseline_fullscreen
            )
        )
        fullScreenButton.contentDescription = getString(
            if (isFullScreen) R.string.exit_full_screen else R.string.enter_full_screen
        )
        updateSystemBars()
    }

    private fun updateSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullScreen || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        private const val STREAM_URL =
            "https://d1gnaphp93fop2.cloudfront.net/videos/multiresolution/rendition_new10.m3u8"
        private const val SEEK_INCREMENT_MILLIS = 5_000L
        private const val KEY_PLAYBACK_POSITION = "playback_position"
        private const val KEY_PLAY_WHEN_READY = "play_when_ready"
        private const val KEY_IS_FULLSCREEN = "is_fullscreen"
        private const val KEY_IS_LOCKED = "is_locked"
    }
}
