package com.example.ecommerce.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = ""
) : Parcelable