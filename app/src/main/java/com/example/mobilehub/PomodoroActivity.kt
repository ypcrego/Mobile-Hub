package com.example.mobilehub

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class PomodoroActivity : AppCompatActivity() {

    private val viewModel: PomodoroViewModel by viewModels()
    private lateinit var soundManager: PomodoroSoundManager

    private lateinit var tvTimer: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnReset: Button
    private lateinit var frogContainer: FrameLayout

    // all spritesheets
    private fun getFrogSprites(): List<Int> {
        val typedArray = resources.obtainTypedArray(R.array.frog_sprites_array)
        val list = mutableListOf<Int>()
        for (i in 0 until typedArray.length()) {
            val resId = typedArray.getResourceId(i, -1)
            if (resId != -1) list.add(resId)
        }
        typedArray.recycle()
        return list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        soundManager = PomodoroSoundManager(this)

        tvTimer = findViewById(R.id.tvTimer)
        btnToggle = findViewById(R.id.btnToggleTimer)
        btnReset = findViewById(R.id.btnResetTimer)
        frogContainer = findViewById(R.id.frogContainer)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.timeString.observe(this) { time ->
            tvTimer.text = time
        }

        viewModel.isRunning.observe(this) { isRunning ->
            btnToggle.text = if (isRunning) getString(R.string.pausar) else getString(R.string.iniciar)
        }

        viewModel.spawnFrogEvent.observe(this) {
            spawnRandomFrog()
        }

        viewModel.croakEvent.observe(this) {
            soundManager.playFrogSpawnSound()
        }

        viewModel.alarmEvent.observe(this) {
            soundManager.playFinalAlarm()
        }
    }

    private fun setupListeners() {
        btnToggle.setOnClickListener { viewModel.toggleTimer() }
        btnReset.setOnClickListener {
            viewModel.resetTimer()
            frogContainer.removeAllViews() // Limpa os sapos da tela
        }
    }

    private fun spawnRandomFrog() {
        val allFrogs = getFrogSprites()
        if (allFrogs.isEmpty()) return

        val randomSprite = allFrogs.random()
        val frogView = FrogView(this, randomSprite)

        frogContainer.post {
            frogView.posX = Random.nextInt(0, (frogContainer.width / 2).coerceAtLeast(1)).toFloat()
            frogView.posY = Random.nextInt(0, (frogContainer.height / 2).coerceAtLeast(1)).toFloat()

            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            frogView.layoutParams = layoutParams
            frogContainer.addView(frogView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}