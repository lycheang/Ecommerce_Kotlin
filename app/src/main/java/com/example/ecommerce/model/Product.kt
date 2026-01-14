package com.example.ecommerce.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    // CHANGED: Renamed 'imageUrl' to 'images' because it is a List
    val images: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val sizes: List<String> = emptyList(),
    val amount: Int = 0,
    val inStock: Boolean = true
) : Parcelable