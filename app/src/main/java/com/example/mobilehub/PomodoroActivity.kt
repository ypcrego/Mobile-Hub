package com.example.mobilehub

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
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

        // TODO frog
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isNightMode) {
            rootLayout.setBackgroundResource(R.drawable.bg_swamp_night)
        } else {
            rootLayout.setBackgroundResource(R.drawable.bg_swamp)
        }

        // TODO frog
        findViewById<View>(R.id.btnTema)?.setOnClickListener {
            if (isNightMode) {
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
            val frogQuantity = frogContainer.childCount

            for (i in 0 until frogQuantity) {
                val delayMs = kotlin.random.Random.nextLong(0, 1000)

                frogContainer.postDelayed({
                    audioManager.playSound(EnumSound.FROG_CROAK)
                }, delayMs)
            }
        }

        viewModel.alarmEvent.observe(this) {
            audioManager.playSound(EnumSound.ALARM_DROP)
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

}