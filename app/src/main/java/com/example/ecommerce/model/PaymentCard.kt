package com.example.ecommerce.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentCard(
    val id: String = "",
    val cardHolder: String = "",
    val cardNumber: String = "",
    val expiryDate: String = ""
) : Parcelable