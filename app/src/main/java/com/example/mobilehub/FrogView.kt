package com.example.mobilehub

import android.content.Context
import android.graphics.BitmapFactory

class FrogView(context: Context, spriteResId: Int) : BaseSpriteView(context) {

    override val naturalmenteOlhaParaDireita = false

    private val totalRows = 4
    private val totalCols = 8
    private val scaleFactor = 6

    init {
        val sheet = BitmapFactory.decodeResource(resources, spriteResId)
        val helper = SpriteSheetHelper(sheet, totalRows, totalCols)

        val masterList = mutableListOf<android.graphics.Bitmap>()


        // 1ª linha: 6 colunas (0 a 5)
        masterList.addAll(helper.getFramesFromRow(0, 0..5))
        // 2ª linha: 5 colunas (0 a 4)
        masterList.addAll(helper.getFramesFromRow(1, 0..4))
        // 3ª linha: 5 colunas (0 a 4)
        masterList.addAll(helper.getFramesFromRow(2, 0..4))
        // 4ª linha: 4 colunas (1 a 4)
        masterList.addAll(helper.getFramesFromRow(3, 1..4))

        this.frames = masterList

        val frameOriginalWidth = sheet.width / totalCols
        val frameOriginalHeight = sheet.height / totalRows

        this.larguraFrameReal = frameOriginalWidth * scaleFactor
        this.alturaFrameReal = frameOriginalHeight * scaleFactor

        this.intervaloFrameMs = 200L
    }
}