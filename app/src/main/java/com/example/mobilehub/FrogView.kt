package com.example.mobilehub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class FrogView(context: Context, spriteResId: Int) : View(context) {

    private val rows = 4
    private val cols = 8
    private val scaleFactor = 6

    private val paint = Paint().apply {
        isFilterBitmap = false
        isAntiAlias = false
    }

    private val dstRect = Rect()
    private val frames: List<Bitmap>

    private var frameAtual = 0
    private var tempoUltimoFrame = 0L
    private val intervaloFrameMs = 200L

    private var estaVivo = false

    var sapoX = 0f
    var sapoY = 0f

    private var velocidadeX = if (Random.nextBoolean()) Random.nextInt(1, 4).toFloat() else -Random.nextInt(1, 4).toFloat()
    private var velocidadeY = if (Random.nextBoolean()) Random.nextInt(1, 4).toFloat() else -Random.nextInt(1, 4).toFloat()

    private var indoParaDireita = velocidadeX > 0

    private val larguraReal: Int
    private val alturaReal: Int

    init {
        val sheet = BitmapFactory.decodeResource(resources, spriteResId)
        val helper = SpriteSheetHelper(sheet, rows, cols)

        val masterList = mutableListOf<Bitmap>()

        // 1ª linha: 6 colunas completas (0 a 5)
        masterList.addAll(helper.getFramesFromRow(0, 0..5))

        // 2ª linha: 5 colunas completas (0 a 4)
        masterList.addAll(helper.getFramesFromRow(1, 0..4))

        // 3ª linha: 5 colunas completas (0 a 4)
        masterList.addAll(helper.getFramesFromRow(2, 0..4))

        // 4ª linha: 4 colunas (1ª e 6ª vazias -> pegamos de 1 a 4)
        masterList.addAll(helper.getFramesFromRow(3, 1..4))

        frames = masterList

        val frameOriginalWidth = sheet.width / cols
        val frameOriginalHeight = sheet.height / rows
        larguraReal = frameOriginalWidth * scaleFactor
        alturaReal = frameOriginalHeight * scaleFactor

        if (frames.isNotEmpty()) {
            frameAtual = Random.nextInt(0, frames.size)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && sapoX == 0f && sapoY == 0f) {
            sapoX = Random.nextInt(0, (w - larguraReal).coerceAtLeast(1)).toFloat()
            sapoY = Random.nextInt(0, (h - alturaReal).coerceAtLeast(1)).toFloat()
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!estaVivo) return
            atualizar()
            invalidate()
            postOnAnimation(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        estaVivo = true
        postOnAnimation(loop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        estaVivo = false
        removeCallbacks(loop)
    }

    private fun atualizar() {
        if (frames.isEmpty() || width == 0 || height == 0) return

        val tempoAtual = System.currentTimeMillis()
        if (tempoAtual - tempoUltimoFrame >= intervaloFrameMs) {
            frameAtual = (frameAtual + 1) % frames.size
            tempoUltimoFrame = tempoAtual
        }

        sapoX += velocidadeX
        sapoY += velocidadeY

        if (sapoX + larguraReal > width) {
            sapoX = (width - larguraReal).toFloat()
            velocidadeX = -abs(velocidadeX)
            indoParaDireita = false
        } else if (sapoX < 0) {
            sapoX = 0f
            velocidadeX = abs(velocidadeX)
            indoParaDireita = true
        }

        if (sapoY + alturaReal > height) {
            sapoY = (height - alturaReal).toFloat()
            velocidadeY = -abs(velocidadeY)
        } else if (sapoY < 0) {
            sapoY = 0f
            velocidadeY = abs(velocidadeY)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frames.isEmpty()) return

        val frame = frames[frameAtual]

        dstRect.set(
            sapoX.toInt(),
            sapoY.toInt(),
            (sapoX + larguraReal).toInt(),
            (sapoY + alturaReal).toInt()
        )

        canvas.save()

        if (indoParaDireita) {
            canvas.scale(-1f, 1f, dstRect.exactCenterX(), dstRect.exactCenterY())
        }

        canvas.drawBitmap(frame, null, dstRect, paint)
        canvas.restore()
    }
}