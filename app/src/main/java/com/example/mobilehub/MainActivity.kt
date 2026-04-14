package com.example.mobilehub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCalculadora = findViewById<Button>(R.id.btn_calculadora)
        val btnPato = findViewById<Button>(R.id.btn_pato)
        val btnPomodoro = findViewById<Button>(R.id.btnPomodoro)

        btnCalculadora.setOnClickListener {
            val intent = Intent(this, CalculadoraActivity::class.java)
            startActivity(intent)
        }

        btnPato.setOnClickListener {
            val intent = Intent(this, PatoActivity::class.java)
            startActivity(intent)
        }

        btnPomodoro.setOnClickListener {
            val intent = Intent(this, PomodoroActivity::class.java)
            startActivity(intent)
        }

    }
}