package com.example.mobilehub

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PomodoroSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro_settings)

        val prefs = getSharedPreferences("PomodoroPrefs", MODE_PRIVATE)
        val etFocus = findViewById<EditText>(R.id.etFocusTime)
        val etBreak = findViewById<EditText>(R.id.etBreakTime)

        // Carrega valores atuais
        etFocus.setText(prefs.getInt("focus_time", 25).toString())
        etBreak.setText(prefs.getInt("break_time", 5).toString())

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            prefs.edit().apply {
                putInt("focus_time", etFocus.text.toString().toIntOrNull() ?: 25)
                putInt("break_time", etBreak.text.toString().toIntOrNull() ?: 5)
                apply()
            }
            finish()
        }

        val btnClose = findViewById<ImageView>(R.id.btnCloseSettings)
        btnClose.setOnClickListener {
            finish()
        }
    }


}