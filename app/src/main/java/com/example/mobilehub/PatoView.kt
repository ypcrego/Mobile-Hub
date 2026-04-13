package com.example.mobilehub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.graphics.Paint

class PatoView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var spritesheet: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ducky_2_spritesheet)

    // Configurações da Spritesheet
    private val numColunas = 6
    private val numLinhas = 4
    private val linhaAndar = 1


    private var estaEmPerigo = false
    private val patoPaint = Paint()

    // Dimensões de um único frame
    private val larguraFrame = spritesheet.width / numColunas
    private val alturaFrame = spritesheet.height / numLinhas

    // Estado da animação
    private var frameAtual = 0
    private var tempoUltimoFrame: Long = 0
    private var intervaloFrameMs = 150 // Velocidade da animação

    // Estado do movimento
    private var patoX = 100f

    var estaParado = false
    private val fatorEscala = 3
    private var patoY = 100f
    private var velocidadeX = 5f

    private var velocidadeY = 5f
    private var indoParaDireita = true

    private val linhaMorto = 3
    private val frameMorto = 1

    var estaVivo: Boolean = true

    fun getXAtual(): Float = patoX
    fun getYAtual(): Float = patoY


    private val srcRect = Rect()
    private val dstRect = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Atualiza o movimento e a animação APENAS se estiver VIVO
        if (estaVivo) {

            if (!estaParado) {
                atualizarPosicaoEPessoa()
            }


            val left = frameAtual * larguraFrame
            val top = linhaAndar * alturaFrame
            val right = left + larguraFrame
            val bottom = top + alturaFrame

            srcRect.set(left, top, right, bottom)
        } else {
            val linhaMorto = 3
            val frameMorto = 1

            val left = frameMorto * larguraFrame
            val top = linhaMorto * alturaFrame
            val right = left + larguraFrame
            val bottom = top + alturaFrame

            srcRect.set(left, top, right, bottom)
        }


        val fatorEscala = 3 // Aumenta o tamanho do sprite
        val desenhoLeft = patoX.toInt()
        val desenhoTop = patoY.toInt()
        val desenhoRight = desenhoLeft + (larguraFrame * fatorEscala)
        val desenhoBottom = desenhoTop + (alturaFrame * fatorEscala)

        dstRect.set(desenhoLeft, desenhoTop, desenhoRight, desenhoBottom)

        //espelhamento
        canvas.save()

        if (!indoParaDireita) {
            canvas.scale(-1f, 1f, dstRect.exactCenterX(), dstRect.exactCenterY())
        }

        canvas.drawBitmap(spritesheet, srcRect, dstRect, patoPaint)

        canvas.restore() // Restaura a tela para o normal

        // 4. O LOOP DA ANIMAÇÃO
        if (estaVivo) {
            invalidate()
        }
    }

    private fun atualizarPosicaoEPessoa() {
        val tempoAtual = System.currentTimeMillis()

        // 1. Lógica da Animação
        if (tempoAtual > tempoUltimoFrame + intervaloFrameMs) {
            frameAtual++
            if (frameAtual >= numColunas) {
                frameAtual = 0
            }
            tempoUltimoFrame = tempoAtual
        }


        if (width == 0 || height == 0) return


        val fatorEscala = 3
        val larguraReal = larguraFrame * fatorEscala
        val alturaReal = alturaFrame * fatorEscala

        // mov diagonal
        patoX += velocidadeX
        patoY += velocidadeY

        // 4. Verificação de Colisões (Bater nas paredes)

        // Parede da Direita
        if (patoX + larguraReal > width) {
            patoX = (width - larguraReal).toFloat()
            velocidadeX = -Math.abs(velocidadeX) // Inverte a velocidade X (vai pra esquerda)
            indoParaDireita = false
        }
        // Parede da Esquerda
        else if (patoX < 0) {
            patoX = 0f
            velocidadeX = Math.abs(velocidadeX) // Inverte a velocidade X (vai pra direita)
            indoParaDireita = true
        }

        // Teto (Em cima)
        if (patoY < 0) {
            patoY = 0f
            velocidadeY = Math.abs(velocidadeY) // Inverte a velocidade Y (vai pra baixo)
        }
        // Chão (Embaixo)
        else if (patoY + alturaReal > height) {
            patoY = (height - alturaReal).toFloat()
            velocidadeY = -Math.abs(velocidadeY) // Inverte a velocidade Y (vai pra cima)
        }
    }

    fun notificarMorte() {
        estaVivo = false
        invalidate()
    }

    fun notificarRenascer() {
        estaVivo = true
        patoX = 100f
        patoY = 100f
        invalidate()
    }
    fun pausar() {
        estaParado = true
    }

    fun retomar() {
        estaParado = false
        invalidate()
    }

    fun getLarguraEscalada(): Float = (larguraFrame * fatorEscala).toFloat()

    fun getCentroXAtual(): Float = patoX + (getLarguraEscalada() / 2f)


    fun getTopoYAtual(): Float = patoY

    fun setModoPerigo() {
        if (!estaEmPerigo) {
            estaEmPerigo = true

            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            patoPaint.colorFilter = ColorMatrixColorFilter(matrix)

            intervaloFrameMs = 300

            velocidadeX = if (velocidadeX > 0) 2f else -2f
            velocidadeY = if (velocidadeY > 0) 2f else -2f

            invalidate()
        }
    }

    fun setModoNormal() {
        if (estaEmPerigo) {
            estaEmPerigo = false

            patoPaint.colorFilter = null

            intervaloFrameMs = 150

            velocidadeX = if (velocidadeX > 0) 5f else -5f
            velocidadeY = if (velocidadeY > 0) 5f else -5f

            invalidate()
        }
    }
}