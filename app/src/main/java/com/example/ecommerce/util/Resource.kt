package com.example.ecommerce.util

// T stands for "Type" (Generic). It means this class can hold ANY data (User, Product, Int, String, etc.)

// Add 'out' before T
sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
    class Unspecified<T> : Resource<T>()
}