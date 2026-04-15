package com.example.mobilehub

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Alterado para AndroidViewModel para acessar o Context (SharedPreferences)
class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("PomodoroPrefs", Context.MODE_PRIVATE)

    // Variáveis de tempo que agora dependem das configurações
    private var totalTimeMs = 25 * 60 * 1000L
    private val totalFrogsToSpawn = 10
    private var spawnIntervalMs = totalTimeMs / totalFrogsToSpawn

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

    private val _hasStarted = MutableLiveData(false)
    val hasStarted: LiveData<Boolean> get() = _hasStarted

    // Novo: Indica se estamos em modo Foco ou Pausa
    private val _isFocusMode = MutableLiveData(true)
    val isFocusMode: LiveData<Boolean> get() = _isFocusMode

    init {
        loadPreferences()
    }

    // Função para carregar os tempos salvos pelo usuário
    fun loadPreferences() {
        // Se o timer estiver rodando, não atualizamos para não dar salto no relógio
        if (_isRunning.value == true) return

        val focusMinutes = prefs.getInt("focus_time", 25)
        val breakMinutes = prefs.getInt("break_time", 5)

        // Define qual tempo carregar baseado no modo atual
        val minutes = if (_isFocusMode.value == true) focusMinutes else breakMinutes

        totalTimeMs = minutes * 60 * 1000L
        timeRemainingMs = totalTimeMs
        spawnIntervalMs = if (totalTimeMs > 0) totalTimeMs / totalFrogsToSpawn else 1000L

        updateTimeText(timeRemainingMs)

        _hasStarted.value = false
    }

    fun toggleTimer() {
        if (_isRunning.value == true) pauseTimer() else startTimer()
    }

    private fun startTimer() {
        _isRunning.value = true
        _hasStarted.value = true
        timer = object : CountDownTimer(timeRemainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMs = millisUntilFinished
                updateTimeText(millisUntilFinished)
                checkFrogSpawn(millisUntilFinished)
                checkPeriodicSound(millisUntilFinished)
            }

            override fun onFinish() {
                _isRunning.value = false
                _hasStarted.value = false
                timeRemainingMs = 0
                updateTimeText(0)
                _alarmEvent.value = Unit

                // Troca automaticamente: se era Foco vira Pausa, e vice-versa
                _isFocusMode.value = !(_isFocusMode.value ?: true)
                loadPreferences()
            }
        }.start()
    }

    private fun checkPeriodicSound(millisUntilFinished: Long) {
        val segundosRestantes = millisUntilFinished / 1000
        if (frogsSpawned > 0 && segundosRestantes > 0 && segundosRestantes % 5 == 0L) {
            _croakEvent.value = Unit
        }
    }

     fun pauseTimer() {
        timer?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()
        frogsSpawned = 0
        _hasStarted.value = false
        loadPreferences() // Recarrega o tempo original
    }

    private fun checkFrogSpawn(millisUntilFinished: Long) {
        val elapsedTime = totalTimeMs - millisUntilFinished
        val expectedFrogs = (elapsedTime / spawnIntervalMs).toInt()

        if (expectedFrogs > frogsSpawned && frogsSpawned < totalFrogsToSpawn) {
            frogsSpawned++
            _spawnFrogEvent.value = Unit
        }
    }

    private fun updateTimeText(millis: Long) {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        _timeString.value = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}