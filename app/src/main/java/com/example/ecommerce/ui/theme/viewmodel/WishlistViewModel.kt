package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Product
import com.example.ecommerce.repository.ProductRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _wishlistProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val wishlistProducts = _wishlistProducts.asStateFlow()

    init {
        getWishlist()
    }

    // ---------- GET LIST ----------
    fun getWishlist() = viewModelScope.launch {
        if (_wishlistProducts.value is Resource.Loading || _wishlistProducts.value.data.isNullOrEmpty()) {
            _wishlistProducts.value = Resource.Loading()
        }
        val result = repository.getWishlistProducts()
        _wishlistProducts.value = result
    }

    // ---------- REMOVE ----------
    fun removeFromWishlist(product: Product) = viewModelScope.launch {
        // 1. Call Repository to delete from Firestore
        val result = repository.removeFromWishlist(product.id)

        // 2. If successful, refresh the list to show the change
        if (result is Resource.Success) {
            getWishlist()
        } else {
            // Optional: Handle error (e.g., show a one-time event for Toast)
        }
    }

    // ---------- UNDO (ADD BACK) ----------
    fun addToWishlist(product: Product) = viewModelScope.launch {
        // 1. Call Repository to add back to Firestore
        val result = repository.toggleWishlist(product.id, isAdd = true)

        // 2. Refresh the list
        if (result is Resource.Success) {
            getWishlist()
        }
    }
}