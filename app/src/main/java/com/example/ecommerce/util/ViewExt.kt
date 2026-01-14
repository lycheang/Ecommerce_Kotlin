package com.example.ecommerce.util

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide

// This function extends ALL ImageViews in your app automatically
fun ImageView.loadBase64OrUrl(data: String?) {
    if (data.isNullOrEmpty()) return

    try {
        // 1. If it's a URL (http...), use Glide
        if (data.startsWith("http")) {
            Glide.with(this.context)
                .load(data)
                .into(this)
        }
        // 2. If it's Base64, decode it manually
        else {
            val cleanString = if (data.contains(",")) data.substringAfter(",") else data
            val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            this.setImageBitmap(bitmap)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}