package com.example.mobilehub

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class DefaultAudioManager private constructor(context: Context) : AudioManager {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<EnumSound, Int>()
    private var alarmPlayer: MediaPlayer? = null
    private var loadedSounds = 0
    private var isSoundsReady = false
    private var currentUiStreamId: Int = 0

    private val appContext = context.applicationContext

    private val soundPoolConfig = mapOf(
        EnumSound.CAT_DAY to R.raw.som_gato_dia,
        EnumSound.CAT_NIGHT to R.raw.som_gato_noite,
        EnumSound.SOM_SAPO_1 to R.raw.som_sapo_1,
        EnumSound.SOM_SAPO_2 to R.raw.som_sapo_2,
        EnumSound.SOM_SAPO_3 to R.raw.som_sapo_3,
        EnumSound.SOM_SAPO_4 to R.raw.som_sapo_4,
        EnumSound.RANDOM_QUACK to R.raw.random_quack,
        EnumSound.QUACK_DROP to R.raw.quack_drop,
    )

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPoolConfig.forEach { (enumSound, resId) ->
            soundMap[enumSound] = soundPool.load(appContext, resId, 1)
        }

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loadedSounds++
                if (loadedSounds == soundPoolConfig.size) {
                    isSoundsReady = true
                }
            }
        }
    }

    override fun playSound(sound: EnumSound) {
        // Long sounds
        if (sound == EnumSound.ALARM_DROP) {
            playQuackDrop()
            return
        }

        // Short sounds
        if (isSoundsReady) {
            soundMap[sound]?.let { soundId ->
                if (sound == EnumSound.CAT_DAY || sound == EnumSound.CAT_NIGHT) {
                    if (currentUiStreamId != 0) soundPool.stop(currentUiStreamId)
                    currentUiStreamId = soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                } else {
                    soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                }
            }
        }
    }

    private fun playQuackDrop() {
        alarmPlayer?.release()
        alarmPlayer = MediaPlayer.create(appContext, R.raw.quack_drop)
        alarmPlayer?.start()

        alarmPlayer?.setOnCompletionListener {
            it.release()
            alarmPlayer = null
        }
    }

    override fun release() {
        soundPool.release()
        alarmPlayer?.release()
    }
    companion object {
        @Volatile
        private var instance: DefaultAudioManager? = null

        fun getInstance(context: Context): DefaultAudioManager {
            return instance ?: synchronized(this) {
                instance ?: DefaultAudioManager(context).also { instance = it }
            }
        }
    }
}