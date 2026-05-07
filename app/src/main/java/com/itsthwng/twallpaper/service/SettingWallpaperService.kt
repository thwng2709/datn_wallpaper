package com.itsthwng.twallpaper.service

import android.os.Environment
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File
import java.io.IOException

class SettingWallpaperService : WallpaperService() {
    /**
     * @see WallpaperService.onCreate
     */
    override fun onCreate() {
        super.onCreate()

        // Save timestamp when live wallpaper service is created (wallpaper is set)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_SET_TIME, System.currentTimeMillis()) }

        Log.d(TAG, "SettingWallpaperService onCreate - Live wallpaper is now active, broadcast sent")
    }

    /**
     * @see WallpaperService.onCreateEngine
     */
    override fun onCreateEngine(): Engine? {
        try {
            return NyanEngine()
        } catch (e: IOException) {
            Log.w(TAG, "Error creating NyanEngine", e)
            stopSelf()
            return null
        }
    }

    val path: File?
        get() {
            val state = Environment.getExternalStorageState()
            val filesDir: File? = if (Environment.MEDIA_MOUNTED == state) {
                // We can read and write the media
                getExternalFilesDir(null)
            } else {
                // Load another directory, probably local memory
                filesDir
            }
            return filesDir
        }

    internal inner class NyanEngine : Engine() {
        private var player: ExoPlayer? = null
        private var currentVideoPath: String? = null
        var mWhen: Int = 0
        var mStart: Long = 0

        override fun onDestroy() {
            releasePlayer()
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                // Release old player and create new one to ensure latest video is loaded
                releasePlayer()
                nyan()
            } else {
                // Release player when not visible to free resources and ensure reload on next visible
                releasePlayer()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (player != null) {
                player!!.setVideoSurface(holder.surface)
            }
            nyan()
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float, xOffsetStep: Float,
            yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int
        ) {
            super.onOffsetsChanged(
                xOffset,
                yOffset,
                xOffsetStep,
                yOffsetStep,
                xPixelOffset,
                yPixelOffset
            )
            nyan()
        }

        fun nyan() {
            try {
                tick()
                val surfaceHolder = getSurfaceHolder()
                if (surfaceHolder == null || surfaceHolder.surface == null) {
                    Log.w(TAG, "SurfaceHolder or Surface is null, skipping nyan()")
                    return
                }

                val pendingVideoPath = "$path/$PENDING_VIDEO_NAME"
                val mainVideoPath = "$path/$MAIN_VIDEO_NAME"
                val pendingFile = File(pendingVideoPath)

                val videoPath = if (isPreview && pendingFile.exists()) {
                    Log.d(TAG, "Preview mode: loading pending video")
                    pendingVideoPath
                } else {
                    mainVideoPath
                }

                // Reuse existing player if it's playing the same video
                if (player != null && videoPath == currentVideoPath) {
                    try {
                        // Just update surface if needed
                        player!!.setVideoSurface(surfaceHolder.surface)
                        if (!player!!.playWhenReady) {
                            player!!.playWhenReady = true
                        }
                        return
                    } catch (e: Exception) {
                        Log.w(TAG, "Error reusing player, will create new one: " + e.message)
                        releasePlayer()
                    }
                }

                // Release old player before creating new one
                releasePlayer()

                // Create new player
                try {
                    player = ExoPlayer.Builder(applicationContext)
                        .build()

                    val mediaItem = MediaItem.fromUri(videoPath.toUri())
                    player!!.setMediaItem(mediaItem)
                    player!!.repeatMode = Player.REPEAT_MODE_ALL
                    player!!.seekTo(0, 0)
                    player!!.playWhenReady = true
                    player!!.volume = 0f
                    player!!.setVideoSurface(surfaceHolder.surface)
                    player!!.prepare()

                    currentVideoPath = videoPath
                    Log.d(TAG, "ExoPlayer created and prepared successfully")
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "OutOfMemoryError creating ExoPlayer: " + e.message)
                    releasePlayer()
                    // Don't retry immediately to avoid crash loop
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating ExoPlayer: " + e.message)
                    releasePlayer()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in nyan(): " + e.message)
            }
        }

        fun tick() {
            if (mWhen.toLong() == -1L) {
                mWhen = 0
                mStart = SystemClock.uptimeMillis()
            }
        }

        private fun releasePlayer() {
            if (player != null) {
                try {
                    player!!.playWhenReady = false
                    player!!.stop()
                    player!!.release()
                    Log.d(TAG, "ExoPlayer released successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing ExoPlayer: " + e.message)
                } finally {
                    player = null
                    currentVideoPath = null
                }
            }
        }
    }

    companion object {
        const val TAG: String = "NYAN"
        private const val PREFS_NAME = "live_wallpaper_prefs"
        private const val KEY_LAST_SET_TIME = "last_set_time"

        // File names cho video
        const val MAIN_VIDEO_NAME = "video.mp4"
        const val PENDING_VIDEO_NAME = "video_pending.mp4"
    }
}