package com.example.ecommerce.model

import com.google.firebase.Timestamp

data class WishList(
    var id: String = "",
    val productId: String = "",
    val addedAt: Timestamp = Timestamp.now()
)
