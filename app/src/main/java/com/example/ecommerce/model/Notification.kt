package com.example.ecommerce.model


data class Notification(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val date: Long = 0,
    val read: Boolean = false
)