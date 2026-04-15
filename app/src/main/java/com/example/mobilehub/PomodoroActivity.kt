package com.example.mobilehub

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlin.random.Random

class PomodoroActivity : AppCompatActivity() {

    private val viewModel: PomodoroViewModel by viewModels()

    private lateinit var tvTimer: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnReset: Button
    private lateinit var frogContainer: FrameLayout
    private lateinit var rootLayout: ConstraintLayout

    private lateinit var audioManager: AudioManager


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

        audioManager = DefaultAudioManager.getInstance(this)

        rootLayout = findViewById(R.id.pomodoro_root)
        tvTimer = findViewById(R.id.tvTimer)
        btnToggle = findViewById(R.id.btnToggleTimer)
        btnReset = findViewById(R.id.btnResetTimer)
        frogContainer = findViewById(R.id.frogContainer)
        val isDay = isDaytime()
        audioManager.startNightAmbience()
        val btnVoltar = findViewById<Button>(R.id.btn_voltar_hub)

        // TODO frog
        if (isDay) {
            rootLayout.setBackgroundResource(R.drawable.bg_swamp)
            val verdeFloresta = ContextCompat.getColor(this, R.color.verde_floresta)
            btnVoltar.setTextColor(verdeFloresta)
            tvTimer.setTextColor(verdeFloresta)
        } else {
            rootLayout.setBackgroundResource(R.drawable.bg_swamp)
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        // TODO frog
        findViewById<View>(R.id.btnTema)?.setOnClickListener {
            val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
                audioManager.playSound(EnumSound.CAT_DAY)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                audioManager.playSound(EnumSound.CAT_NIGHT)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        findViewById<Button>(R.id.btn_voltar_hub).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, PomodoroSettingsActivity::class.java)
            startActivity(intent)
        }

        setupObservers()
        setupListeners()
    }

    private fun isDaytime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 6..17 // 06:00 às 17:59
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
            val frogQuantity = frogContainer.childCount

            // Lista com os seus novos sons de sapo
            val frogSounds = listOf(
                EnumSound.SOM_SAPO_1,
                EnumSound.SOM_SAPO_2,
                EnumSound.SOM_SAPO_3,
                EnumSound.SOM_SAPO_4
            )

            for (i in 0 until frogQuantity) {
                // Sorteia um delay entre 0 e 2 segundos para cada sapo
                val delayMs = kotlin.random.Random.nextLong(0, 2000)
                // Sorteia qual dos 4 sons esse sapo específico vai emitir
                val randomSound = frogSounds.random()

                frogContainer.postDelayed({
                    audioManager.playSound(randomSound)
                }, delayMs)
            }
        }

        viewModel.alarmEvent.observe(this) {
            audioManager.playSound(EnumSound.ALARM_DROP)
        }

        viewModel.isRunning.observe(this) { isRunning ->
            val hasStarted = viewModel.hasStarted.value ?: false

            if (isRunning) {
                btnToggle.text = getString(R.string.pausar)
            } else {
                btnToggle.text = if (hasStarted) getString(R.string.continuar) else getString(R.string.iniciar)
            }
        }

        viewModel.hasStarted.observe(this) { hasStarted ->
            if (viewModel.isRunning.value == false) {
                btnToggle.text = if (hasStarted) getString(R.string.continuar) else getString(R.string.iniciar)
            }
        }
    }

    private fun setupListeners() {
        btnToggle.setOnClickListener { viewModel.toggleTimer() }
        btnReset.setOnClickListener {
            viewModel.resetTimer()
            frogContainer.removeAllViews() // Remove frogs from screen
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

    override fun onResume() {
        super.onResume()
        viewModel.loadPreferences(forceReset = false)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.stopAmbience() 
    }



}