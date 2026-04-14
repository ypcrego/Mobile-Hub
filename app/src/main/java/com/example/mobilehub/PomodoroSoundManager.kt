// PomodoroSoundManager.kt
package com.example.mobilehub

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class PomodoroSoundManager(private val context: Context) {

    private var soundPool: SoundPool
    private var frogSoundId: Int = 0
    private var alarmPlayer: MediaPlayer? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Permite até 10 sapos coaxando ao mesmo tempo
            .setAudioAttributes(audioAttributes)
            .build()

        // TODO alterar som sapo
        frogSoundId = soundPool.load(context, R.raw.som_gato_dia, 1) // Substitua pelo som do sapo
    }

    fun playFrogSpawnSound() {
        soundPool.play(frogSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playFinalAlarm() {
        // TODO som alarme
        alarmPlayer = MediaPlayer.create(context, R.raw.quack_drop)
        alarmPlayer?.start()
    }

    fun release() {
        soundPool.release()
        alarmPlayer?.release()
    }
}