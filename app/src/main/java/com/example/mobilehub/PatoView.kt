package com.example.mobilehub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet

class PatoView(context: Context, attrs: AttributeSet?) : BaseSpriteView(context, attrs) {


    override val naturalmenteOlhaParaDireita = true
    private val totalRows = 4
    private val totalCols = 6
    private val scaleFactor = 3

    private lateinit var walkFrames: List<Bitmap>
    private lateinit var deathFrame: Bitmap

    init {
        val sheet = BitmapFactory.decodeResource(resources, R.drawable.ducky_2_spritesheet)
        val helper = SpriteSheetHelper(sheet, totalRows, totalCols)

        walkFrames = helper.getFramesFromRow(1, 0 until totalCols)

        val deathFrames = helper.getFramesFromRow(3, 1..1)
        deathFrame = deathFrames[0]

        this.frames = walkFrames

        val frameWidth = sheet.width / totalCols
        val frameHeight = sheet.height / totalRows

        this.larguraFrameReal = frameWidth * scaleFactor
        this.alturaFrameReal = frameHeight * scaleFactor

        // Posição e velocidade iniciais
        this.posX = 100f
        this.posY = 100f
        this.velX = 5f
        this.velY = 5f
        this.intervaloFrameMs = 150L
    }

    fun setModoPerigo() {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        this.paint.colorFilter = ColorMatrixColorFilter(matrix)
        this.intervaloFrameMs = 300L
        val novaVel = 2f
        this.velX = if (this.velX >= 0) novaVel else -novaVel
        this.velY = if (this.velY >= 0) novaVel else -novaVel
    }

    fun setModoNormal() {
        this.paint.colorFilter = null
        this.intervaloFrameMs = 150L
        val novaVel = 5f
        this.velX = if (this.velX >= 0) novaVel else -novaVel
        this.velY = if (this.velY >= 0) novaVel else -novaVel
    }

    fun notificarMorte() {
        this.estaVivo = false
        this.frames = listOf(deathFrame) // Trava a animação no frame de morte
        invalidate()
    }

    fun notificarRenascer() {
        this.estaVivo = true
        this.frames = walkFrames
        this.posX = 100f
        this.posY = 100f
        this.retomar()
        invalidate()
    }

    fun pausar() {
        this.isPausado = true
    }

    fun retomar() {
        this.isPausado = false
    }

    // Helpers usados pela PatoActivity para posicionar os corações/milhos
    fun getCentroXAtual(): Float = posX + (larguraFrameReal / 2f)
    fun getTopoYAtual(): Float = posY
}