package com.example.mobilehub

interface AudioManager {
    fun playSound(sound: EnumSound)
    fun release()

    fun startNightAmbience()
    fun stopAmbience()
}