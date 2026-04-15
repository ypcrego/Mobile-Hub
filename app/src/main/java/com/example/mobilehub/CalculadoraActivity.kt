package com.example.mobilehub

import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculadoraActivity : AppCompatActivity() {
    private lateinit var tvDisplay: TextView

    private var currentInput: String = ""
    private var operand: Double? = null
    private var pendingOp: String? = null

    private lateinit var audioManager: AudioManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        audioManager = DefaultAudioManager.getInstance(this)

        // TextView de display
        tvDisplay = findViewById(R.id.txtResultado)

        // Botões de dígitos
        val digits = listOf(
            "0" to R.id.btn0,
            "1" to R.id.btn1,
            "2" to R.id.btn2,
            "3" to R.id.btn3,
            "4" to R.id.btn4,
            "5" to R.id.btn5,
            "6" to R.id.btn6,
            "7" to R.id.btn7,
            "8" to R.id.btn8,
            "9" to R.id.btn9,
            "." to R.id.btnPonto
        )
        digits.forEach { (digit, id) ->
            findViewById<View>(id)?.setOnClickListener { appendDigit(digit) }
        }

        val unaryOps = listOf(
            "sin" to R.id.btnSin,
            "cos" to R.id.btnCos,
            "tan" to R.id.btnTan,
            "ln" to R.id.btnLn,
            "log" to R.id.btnLog,
            "√" to R.id.btnSqrt
        )
        unaryOps.forEach { (op, id) ->
            findViewById<View>(id)?.setOnClickListener { onUnaryOperator(op) }
        }

        // Botões de operações
        val ops = listOf(
            "+" to R.id.btnSomar,
            "-" to R.id.btnSubtrair,
            "×" to R.id.btnMultiplicar,
            "÷" to R.id.btnDividir,
            "^" to R.id.btnPower
        )
        ops.forEach { (op, id) ->
            val view = findViewById<View>(id)
            fixPixelArt(id)
            view?.setOnClickListener { onOperator(op) }
        }

        // Botão igual
        findViewById<View>(R.id.btnIgual)?.setOnClickListener { onEquals() }

        // Botão limpar tudo
        findViewById<View>(R.id.btnClear)?.setOnClickListener { clearAll() }

        // Botão backspace
        findViewById<View>(R.id.btnBackspace)?.setOnClickListener { backspace() }

        findViewById<View>(R.id.btnPi)?.setOnClickListener {
            currentInput = PI.toString()
            updateDisplay()
        }

        findViewById<View>(R.id.btnTema)?.setOnClickListener {
            val isNightMode = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

            if (isNightMode) {
                audioManager.playSound(EnumSound.CAT_DAY)

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            } else {
                audioManager.playSound(EnumSound.CAT_NIGHT)

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            }
        }

        findViewById<Button>(R.id.btn_voltar_hub).setOnClickListener {
            finish()
        }

        fixPixelArt(R.id.btnIgual, R.id.btnClear, R.id.btnBackspace)

        updateDisplay()
    }

    private fun appendDigit(d: String) {
        if (d == "." && currentInput.contains(".")) return
        currentInput = if (currentInput == "0") d else currentInput + d
        updateDisplay()
    }

    private fun onOperator(op: String) {
        if (currentInput.isNotEmpty()) {
            val value = currentInput.toDoubleOrNull()
            if (value != null) {
                if (operand == null) operand = value
                else operand = performOperation(operand!!, value, pendingOp)
            }
            currentInput = ""
        }
        pendingOp = op
        updateDisplay()
    }

    private fun onEquals() {
        if (operand != null && currentInput.isNotEmpty()) {
            val value = currentInput.toDoubleOrNull() ?: return
            val result = performOperation(operand!!, value, pendingOp)
            operand = null
            pendingOp = null
            currentInput = result.toString()
            updateDisplay()
        }
    }

    private fun performOperation(a: Double, b: Double, op: String?): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "^" -> a.pow(b)
            "÷" -> if (b == 0.0) {
                Toast.makeText(this, "Apesar da fome por conhecimento, você ainda não pode dividir por zero.", Toast.LENGTH_SHORT).show()
                a
            } else a / b
            else -> b
        }
    }

    private fun onUnaryOperator(op: String) {
        val value = currentInput.toDoubleOrNull() ?: operand ?: return
        val result = when (op) {
            "sin" -> sin(value)
            "cos" -> cos(value)
            "tan" -> tan(value)
            "ln" -> ln(value)
            "log" -> log10(value)
            "√" -> sqrt(value)
            else -> value
        }

        if (result.isNaN() || result.isInfinite()) {
            Toast.makeText(this, "Operação miautemática inválida para este número.", Toast.LENGTH_SHORT).show()
            return
        }

        currentInput = result.toString()
        if (operand == value) {
            operand = null
            pendingOp = null
        }
        updateDisplay()
    }

    private fun clearAll() {
        currentInput = ""
        operand = null
        pendingOp = null
        updateDisplay()
    }

    private fun backspace() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        val text = buildString {
            if (operand != null) {
                // Removes ".0" if it's a whole number for a cleaner visual
                val opStr = if (operand!! % 1 == 0.0) operand!!.toLong().toString() else operand.toString()
                append(opStr)
                if (pendingOp != null) {
                    append(" $pendingOp ")
                }
            }
            if (currentInput.isNotEmpty()) {
                append(currentInput)
            }
        }
        tvDisplay.text = text.ifEmpty { "0" }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentInput", currentInput)
        outState.putDouble("operand", operand ?: Double.NaN)
        outState.putString("pendingOp", pendingOp)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentInput = savedInstanceState.getString("currentInput", "")
        val opnd = savedInstanceState.getDouble("operand", Double.NaN)
        operand = if (opnd.isNaN()) null else opnd
        pendingOp = savedInstanceState.getString("pendingOp")
        updateDisplay()
    }

    private fun fixPixelArt(vararg ids: Int) {
        for (id in ids) {
            val view = findViewById<View>(id)
            (view as? ImageView)?.let { imgView ->
                (imgView.drawable as? BitmapDrawable)?.isFilterBitmap = false
            }
        }
    }

}