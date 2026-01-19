package com.sd.nithyadharma.util

import android.content.Context
import android.media.SoundPool
import android.util.Log
import com.sd.nithyadharma.R

class SoundManager private constructor(context: Context) {
    private var soundPool: SoundPool? = null
    private var bellSoundId = 0
    private var isSoundLoaded = false
    private val appContext: Context = context.applicationContext
    @Volatile private var isInitializing = false

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        if (isInitializing) return
        isInitializing = true

        try {
            soundPool?.release() // Release existing SoundPool if any
            soundPool = SoundPool.Builder().setMaxStreams(2).build()
            soundPool?.setOnLoadCompleteListener { _, soundId, status ->
                if (status == 0 && soundId == bellSoundId) {
                    isSoundLoaded = true
                    isInitializing = false
//                    Log.d(TAG, "Sound loaded successfully, soundId: $soundId")
                } else {
                    isSoundLoaded = false
                    Log.e(TAG, "Sound load failed, status: $status")
                }
            }
            bellSoundId = soundPool?.load(appContext, R.raw.smallkuduk, 1) ?: 0
//            Log.d(TAG, "Initialized SoundPool and started loading sound")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
            isSoundLoaded = false
            isInitializing = false
        }
    }

    fun playKuduk() {
        if (!isSoundLoaded || soundPool == null) {
            Log.w(TAG, "Sound not loaded or SoundPool null, reinitializing")
            initializeSoundPool()
            return
        }
        try {
            soundPool?.play(bellSoundId, 1f, 1f, 0, 0, 1f)
//            Log.d(TAG, "Playing kuduk sound")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound", e)
            isSoundLoaded = false
            initializeSoundPool() // Retry initialization
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            isSoundLoaded = false
            bellSoundId = 0
            synchronized(SoundManager::class.java) {
                instance = null
            }
            Log.d(TAG, "SoundPool released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool", e)
        }
    }

    companion object {
        private const val TAG = "SoundManager"
        @Volatile private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager =
            instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
    }
}
