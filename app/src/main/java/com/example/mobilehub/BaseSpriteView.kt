package com.example.mobilehub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave

abstract class BaseSpriteView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    public var posX = 0f
    public var posY = 0f
    protected var velX = 5f
    protected var velY = 5f
    protected var indoParaDireita = true

    abstract val naturalmenteOlhaParaDireita: Boolean
    protected var estaVivo = true

    protected var frameAtual = 0
    protected var tempoUltimoFrame = 0L
    protected var intervaloFrameMs = 150L

    protected val paint = Paint().apply {
        isFilterBitmap = false
        isAntiAlias = false
    }
    protected val dstRect = Rect()

    // Lista de bitmaps/frames que a classe filha vai preencher
    protected var frames = listOf<Bitmap>()
    protected var larguraFrameReal = 0
    protected var alturaFrameReal = 0

    // loop comum de animação
    private val loop = object : Runnable {
        override fun run() {
            if (!estaVivo) return
            atualizarFisicaEAnimacao()
            invalidate()
            postOnAnimation(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postOnAnimation(loop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(loop)
    }

    private fun atualizarFisicaEAnimacao() {
        if (frames.isEmpty() || width == 0 || height == 0) return

        val tempoAtual = System.currentTimeMillis()
        if (tempoAtual - tempoUltimoFrame >= intervaloFrameMs) {
            frameAtual = (frameAtual + 1) % frames.size
            tempoUltimoFrame = tempoAtual
        }

        posX += velX
        posY += velY

        // Lógica de colisão genérica
        if (posX + larguraFrameReal > width) {
            posX = (width - larguraFrameReal).toFloat()
            velX = -Math.abs(velX)
            indoParaDireita = false
        } else if (posX < 0) {
            posX = 0f
            velX = Math.abs(velX)
            indoParaDireita = true
        }

        if (posY + alturaFrameReal > height) {
            posY = (height - alturaFrameReal).toFloat()
            velY = -Math.abs(velY)
        } else if (posY < 0) {
            posY = 0f
            velY = Math.abs(velY)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frames.isEmpty()) return

        val frame = frames[frameAtual]
        dstRect.set(posX.toInt(), posY.toInt(), (posX + larguraFrameReal).toInt(), (posY + alturaFrameReal).toInt())

        canvas.withSave {
            if (indoParaDireita != naturalmenteOlhaParaDireita) {
                canvas.scale(-1f, 1f, dstRect.exactCenterX(), dstRect.exactCenterY())
            }
            drawBitmap(frame, null, dstRect, paint)
        }
    }
}