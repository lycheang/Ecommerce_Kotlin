package com.example.ecommerce.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val address: Address = Address(),
    val paymentMethod: String = "",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val date: Long = System.currentTimeMillis()

): Parcelable