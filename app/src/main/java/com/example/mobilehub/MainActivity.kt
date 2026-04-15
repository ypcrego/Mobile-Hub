package com.example.mobilehub

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var btnTema: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTema = findViewById(R.id.btnTema)

        val cardPato = findViewById<CardView>(R.id.card_pato)
        val cardCalculadora = findViewById<CardView>(R.id.card_calculadora)
        val cardPomodoro = findViewById<CardView>(R.id.card_pomodoro)


        btnTema.setOnClickListener {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        cardPato.setOnClickListener {
            val intent = Intent(this, PatoActivity::class.java)
            startActivity(intent)
        }

        cardCalculadora.setOnClickListener {
            val intent = Intent(this, CalculadoraActivity::class.java)
            startActivity(intent)
        }

        cardPomodoro.setOnClickListener {
            val intent = Intent(this, PomodoroActivity::class.java)
            startActivity(intent)
        }

    }
}