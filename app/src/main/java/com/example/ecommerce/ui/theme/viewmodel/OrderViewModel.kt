package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Order
import com.example.ecommerce.repository.OrderRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    // List of Orders
    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    // Single Order Details
    private val _orderDetails = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val orderDetails = _orderDetails.asStateFlow()

    // Fetch list immediately
    init {
        getOrders()
    }

    fun getOrders() = viewModelScope.launch {
        _allOrders.value = Resource.Loading()
        _allOrders.value = repository.getUserOrders()
    }

    fun getOrderDetails(orderId: String) = viewModelScope.launch {
        _orderDetails.value = Resource.Loading()
        _orderDetails.value = repository.getOrderById(orderId)
    }
}