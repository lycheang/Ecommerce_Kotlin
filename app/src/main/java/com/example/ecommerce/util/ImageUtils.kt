package com.example.ecommerce.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.min

object ImageUtils {

    fun getCompressedBytes(context: Context, uri: Uri): ByteArray? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // 1. Force Resize & Crop to 640x640
            val squareBitmap = cropToSquare(originalBitmap, 640)

            // 2. Compress to JPEG (Quality 75% is a good balance)
            val outputStream = ByteArrayOutputStream()
            squareBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

            return outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun cropToSquare(source: Bitmap, size: Int): Bitmap {
        val width = source.width
        val height = source.height

        // Calculate the scale to ensure the smallest side fits the target size (640)
        // This ensures we fill the square without black bars
        val scale = size.toFloat() / min(width, height)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Create the scaled bitmap (it might be 640x900 or 1000x640 now)
        val scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, true)

        // Calculate crop position (Center)
        val x = (newWidth - size) / 2
        val y = (newHeight - size) / 2

        // Crop the center 640x640
        return Bitmap.createBitmap(scaledBitmap, x, y, size, size)
    }
}