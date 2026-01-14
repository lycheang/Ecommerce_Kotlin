package com.example.ecommerce.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: String = "",
    val fullName: String = "",
    val addressLine: String = "",
    val phoneNumber: String = "",
    val isDefault: Boolean = false
) : Parcelable {
    override fun toString(): String = "$fullName, $addressLine\n$phoneNumber"
}