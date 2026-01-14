package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Address
import com.example.ecommerce.model.CartItem
import com.example.ecommerce.model.Product
import com.example.ecommerce.repository.CartRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<Resource<List<CartItem>>>(Resource.Loading())
    val cartItems = _cartItems.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal = _cartTotal.asStateFlow()

    // State to track if order was successful
    private val _placeOrderStatus = MutableStateFlow<Resource<String>?>(null)
    val placeOrderStatus = _placeOrderStatus.asStateFlow()

    init {
        getCartItems()
    }

    private fun getCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItemsStream().collectLatest { resource ->
                _cartItems.value = resource
                if (resource is Resource.Success) {
                    val items = resource.data ?: emptyList()
                    val newTotal = items.sumOf { it.price * it.quantity }
                    _cartTotal.value = newTotal
                }
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartRepository.addToCart(product)
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            val currentStock = cartRepository.getProductStock(item.id)
            if (item.quantity < currentStock) {
                cartRepository.updateQuantity(item.id, item.quantity + 1)
            }
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                cartRepository.updateQuantity(item.id, item.quantity - 1)
            }
        }
    }

    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(cartItemId)
        }
    }

    // --- FIX: USE REPOSITORY ---
    fun placeOrder(totalAmount: Double, address: Address, cartItems: List<CartItem>) {
        viewModelScope.launch {
            _placeOrderStatus.value = Resource.Loading()

            // Call Repository to handle Firestore logic
            val result = cartRepository.placeOrder(totalAmount, address, cartItems)

            _placeOrderStatus.value = result
        }
    }
}