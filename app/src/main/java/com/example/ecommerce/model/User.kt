package com.example.ecommerce.model

import com.google.firebase.Timestamp

data class User(
    var uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "USER",
    val createdAt: Timestamp = Timestamp.now()
)
