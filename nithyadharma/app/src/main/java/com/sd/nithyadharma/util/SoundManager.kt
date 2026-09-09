package com.sd.nithyadharma.util

import androidx.annotation.RawRes
import com.sd.nithyadharma.R
import android.content.Context
import android.media.SoundPool
import android.util.Log

enum class AppSound(@RawRes val resId: Int) {
    KUDUK(R.raw.smallkuduk),
    SMALL_BELL(R.raw.nd_bell), // Add your new audio file here
    THREE_BELLS(R.raw.nd_temple_bell)
}

class SoundManager private constructor(context: Context) {
    private var soundPool: SoundPool? = null
    private val soundIdMap = mutableMapOf<AppSound, Int>()
    private val loadedSounds = mutableSetOf<Int>()
    private val pendingPlays = mutableSetOf<AppSound>()
    private val appContext: Context = context.applicationContext
    @Volatile private var isInitializing = false

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        if (isInitializing) return
        isInitializing = true

        try {
            soundPool?.release()
            soundPool = SoundPool.Builder().setMaxStreams(4).build()

            soundPool?.setOnLoadCompleteListener { _, soundId, status ->
                if (status == 0) {
                    loadedSounds.add(soundId)

                    val sound = soundIdMap.entries.find { it.value == soundId }?.key
                    if (sound != null && pendingPlays.contains(sound)) {
                        pendingPlays.remove(sound)
                        soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
                    }
                } else {
                    Log.e(TAG, "Failed to load soundId: $soundId, status: $status")
                }
            }

            AppSound.entries.forEach { sound ->
                val soundId = soundPool?.load(appContext, sound.resId, 1) ?: 0
                if (soundId != 0) {
                    soundIdMap[sound] = soundId
                }
            }
            isInitializing = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
            isInitializing = false
        }
    }

    fun play(sound: AppSound) {
        val soundId = soundIdMap[sound]
        if (soundId != null && loadedSounds.contains(soundId) && soundPool != null) {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        } else {
            pendingPlays.add(sound)
            if (soundPool == null) {
                initializeSoundPool()
            }
        }
    }

    fun playKuduk() = play(AppSound.KUDUK)
    fun playSmallBell() = play(AppSound.SMALL_BELL)
    fun playThreeBells() = play(AppSound.THREE_BELLS)

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundIdMap.clear()
            loadedSounds.clear()
            pendingPlays.clear()
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