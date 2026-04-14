// PomodoroViewModel.kt
package com.example.mobilehub

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PomodoroViewModel : ViewModel() {

    // Tempo total do pomodoro (ex: 25 min = 1500000 ms).
    private val totalTimeMs = 30000L
    private val totalFrogsToSpawn = 10
    private val spawnIntervalMs = totalTimeMs / totalFrogsToSpawn

    private var timer: CountDownTimer? = null
    private var timeRemainingMs = totalTimeMs
    private var frogsSpawned = 0

    private val _timeString = MutableLiveData<String>()
    val timeString: LiveData<String> get() = _timeString

    private val _spawnFrogEvent = MutableLiveData<Unit>()
    val spawnFrogEvent: LiveData<Unit> get() = _spawnFrogEvent

    private val _alarmEvent = MutableLiveData<Unit>()
    val alarmEvent: LiveData<Unit> get() = _alarmEvent

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> get() = _isRunning

    private val _croakEvent = MutableLiveData<Unit>()
    val croakEvent: LiveData<Unit> get() = _croakEvent

    init {
        updateTimeText(totalTimeMs)
    }

    fun toggleTimer() {
        if (_isRunning.value == true) pauseTimer() else startTimer()
    }

    private fun startTimer() {
        _isRunning.value = true
        timer = object : CountDownTimer(timeRemainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMs = millisUntilFinished
                updateTimeText(millisUntilFinished)
                checkFrogSpawn(millisUntilFinished)
                checkPeriodicSound(millisUntilFinished)
            }

            override fun onFinish() {
                _isRunning.value = false
                timeRemainingMs = 0
                updateTimeText(0)
                _alarmEvent.value = Unit // Dispara alarme
            }
        }.start()
    }

    private fun checkPeriodicSound(millisUntilFinished: Long) {
        val segundosRestantes = millisUntilFinished / 1000

        // Se já existe algum sapo na tela, a cada 5 segundos ele emite o barulho
        if (frogsSpawned > 0 && segundosRestantes % 5 == 0L) {
            _croakEvent.value = Unit
        }
    }

    private fun pauseTimer() {
        timer?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()
        timeRemainingMs = totalTimeMs
        frogsSpawned = 0
        updateTimeText(totalTimeMs)
    }

    private fun checkFrogSpawn(millisUntilFinished: Long) {
        val elapsedTime = totalTimeMs - millisUntilFinished
        val expectedFrogs = (elapsedTime / spawnIntervalMs).toInt()

        if (expectedFrogs > frogsSpawned) {
            frogsSpawned++
            _spawnFrogEvent.value = Unit
        }
    }

    private fun updateTimeText(millis: Long) {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        _timeString.value = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}