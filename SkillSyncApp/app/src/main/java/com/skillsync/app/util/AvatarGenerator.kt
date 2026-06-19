package com.skillsync.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import kotlin.math.abs

object AvatarGenerator {
    
    // Some nice material colors for avatar backgrounds
    private val colors = listOf(
        "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", 
        "#5C6BC0", "#42A5F5", "#29B6F6", "#26C6DA", 
        "#26A69A", "#66BB6A", "#9CCC65", "#D4E157", 
        "#FFCA28", "#FFA726", "#FF7043", "#8D6E63"
    )

    fun generateAvatar(name: String, size: Int = 200): Bitmap {
        val cleanName = name.trim()
        val initial = if (cleanName.isNotEmpty()) cleanName.substring(0, 1).uppercase() else "?"
        
        // Pick a consistent color based on the name string
        val colorIndex = abs(cleanName.hashCode()) % colors.size
        val backgroundColor = Color.parseColor(colors[colorIndex])

        // Create bitmap and canvas
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

        // Draw text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = size * 0.5f // Text size is half the circle size
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Calculate vertical center to draw text
        val bounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, bounds)
        val textHeight = bounds.height()
        val yOffset = textHeight / 2f - bounds.bottom
        val xPos = size / 2f
        val yPos = size / 2f + yOffset

        canvas.drawText(initial, xPos, yPos, textPaint)

        return bitmap
    }
}
