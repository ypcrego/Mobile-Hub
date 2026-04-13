package com.example.mobilehub

import android.app.Application
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel

class SomViewModel(application: Application) : AndroidViewModel(application) {
    private var soundPool: SoundPool? = null
    private var daySoundId: Int = 0
    private var nightSoundId: Int = 0
    private var isSoundsReady = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        daySoundId = soundPool!!.load(application, R.raw.som_gato_dia, 1)
        nightSoundId = soundPool!!.load(application, R.raw.som_gato_noite, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isSoundsReady = true
        }
    }

    fun playDaySound() {
        if (isSoundsReady) soundPool?.play(daySoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playNightSound() {
        if (isSoundsReady) soundPool?.play(nightSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    override fun onCleared() {
        super.onCleared()
        soundPool?.release()
        soundPool = null
    }
}