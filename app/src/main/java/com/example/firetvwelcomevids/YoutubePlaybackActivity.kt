package com.example.firetvwelcomevids

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


/** Loads [PlaybackVideoFragment]. */
class YoutubePlaybackActivity : FragmentActivity() {

    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var youTubePlayerRef: YouTubePlayer
    private lateinit var tracker:YouTubePlayerTracker;

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)

        tracker = YouTubePlayerTracker()

        if (savedInstanceState == null) {
            val movie =
                intent.getSerializableExtra(MainActivity.MOVIE) as Movie

            youTubePlayerView = findViewById(R.id.youtube_player_view)
            youTubePlayerView.requestFocus();

            lifecycle.addObserver(youTubePlayerView)
            youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.d("YoutubePlaybackActivity", "onReady")
                    youTubePlayerRef = youTubePlayer
                    movie.videoUrl?.let { youTubePlayer.loadVideo(it, 0f) }
                    youTubePlayer.play()
                    youTubePlayerRef.addListener(tracker)
                }
            })
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {

        youTubePlayerView.requestFocus();
        val keyCode = event?.keyCode
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (tracker.state == PlayerConstants.PlayerState.PLAYING) {
                youTubePlayerRef.pause()
            } else {
                youTubePlayerRef.play()
            }
            Log.i(TAG, "dispatchKeyEvent: ${tracker.state}")
//            Toast.makeText(this, "isPlaying: $isPlaying, keyCode: $keyCode", Toast.LENGTH_SHORT).show()
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            youTubePlayerRef.seekTo(tracker.currentSecond.minus(5f))
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            youTubePlayerRef.seekTo(tracker.currentSecond.plus(5f))
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(youTubePlayerView)
    }
}