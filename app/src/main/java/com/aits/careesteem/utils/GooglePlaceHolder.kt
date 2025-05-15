package com.aits.careesteem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.aits.careesteem.R

class GooglePlaceHolder {
    fun getInitialsDouble(first: String?, last: String?): String {
        val firstInitial = first?.firstOrNull()?.uppercaseChar() ?: ""
        val lastInitial = last?.firstOrNull()?.uppercaseChar() ?: ""
        return "$firstInitial$lastInitial"
    }

    fun getInitialsSingle(fullName: String?): String {
        if (fullName.isNullOrBlank()) return ""
        val parts = fullName.trim().split("\\s+".toRegex())
        val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar() ?: ""
        val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar() ?: ""
        return "$first$second"
    }

    fun createInitialsAvatar(context: Context, initials: String): Bitmap {
        val size = 100
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)

        val paintCircle = Paint().apply {
            color = "#279989".toColorInt() // or random color
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCircle)

        val loraTypeface = ResourcesCompat.getFont(context, R.font.lora_bold)

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.create(loraTypeface, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val xPos = size / 2f
        val yPos = (canvas.height / 2f) - ((paintText.descent() + paintText.ascent()) / 2)
        canvas.drawText(initials, xPos, yPos, paintText)

        return bitmap
    }
}