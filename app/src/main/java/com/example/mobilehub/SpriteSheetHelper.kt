package com.example.mobilehub

import android.graphics.Bitmap

class SpriteSheetHelper(
    private val sheet: Bitmap,
    private val rows: Int,
    private val cols: Int
) {

    /**
     * Extrai os frames de uma spritesheet.
     */
    fun getFramesFromRow(row: Int, colRange: IntRange): List<Bitmap> {
        val frames = mutableListOf<Bitmap>()

        val frameWidthExact = sheet.width.toFloat() / cols
        val frameHeightExact = sheet.height.toFloat() / rows

        for (col in colRange) {
            val x = (col * frameWidthExact).toInt()
            val y = (row * frameHeightExact).toInt()

            val frame = Bitmap.createBitmap(
                sheet,
                x,
                y,
                frameWidthExact.toInt(),
                frameHeightExact.toInt()
            )
            frames.add(frame)
        }
        return frames
    }
}