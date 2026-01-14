package com.example.ecommerce.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    var id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val productName: String = "", // <--- ADD THIS so you can show it in the list
    val rating: Float = 0f,
    val comment: String = "",
    val date: Long = System.currentTimeMillis()
) : Parcelable