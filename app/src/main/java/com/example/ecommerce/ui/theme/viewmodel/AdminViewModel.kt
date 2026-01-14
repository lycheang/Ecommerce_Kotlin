package com.example.ecommerce.ui.theme.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Category
import com.example.ecommerce.model.Order
import com.example.ecommerce.model.Product
import com.example.ecommerce.model.User
import com.example.ecommerce.repository.OrderRepository
import com.example.ecommerce.repository.ProductRepository
import com.example.ecommerce.util.ImageUtils
import com.example.ecommerce.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val firestore: FirebaseFirestore,
    private val app: Application // <--- 1. Inject Application Context
) : AndroidViewModel(app) { // <--- 2. Inherit AndroidViewModel

    // --- STATE ---
    var selectedImageUri: Uri? = null
    var currentProduct: Product? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _users = MutableStateFlow<Resource<List<User>>>(Resource.Unspecified())
    val users = _users.asStateFlow()

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    // Single State for Order Update Status
    private val _updateStatus = MutableStateFlow<Resource<Boolean>?>(null)
    val updateStatus = _updateStatus.asStateFlow()

    private val _adminEvents = Channel<AdminEvent>()
    val adminEvents = _adminEvents.receiveAsFlow()

    init {
        loadCategories()
        loadProducts()
    }

    // ==========================================
    //              PRODUCTS LOGIC
    // ==========================================
    fun loadProducts() = viewModelScope.launch {
        _isLoading.value = true
        when (val result = repository.getProducts()) {
            is Resource.Success -> _products.value = result.data ?: emptyList()
            is Resource.Error -> sendEvent(AdminEvent.ShowToast(result.message ?: "Error"))
            else -> Unit
        }
        _isLoading.value = false
    }

    // --- UPDATED SAVE FUNCTION ---
    fun saveProduct(product: Product) = viewModelScope.launch {
        _isLoading.value = true

        // 1. Convert Uri -> Compressed ByteArray (640x640)
        val imageBytes: ByteArray? = selectedImageUri?.let { uri ->
            ImageUtils.getCompressedBytes(app.applicationContext, uri)
        }

        // 2. Pass the BYTES to the repository (not the Uri)
        val result = repository.saveProduct(product, imageBytes)

        _isLoading.value = false

        when (result) {
            is Resource.Success -> {
                sendEvent(AdminEvent.ShowToast("Saved successfully"))
                selectedImageUri = null
                sendEvent(AdminEvent.NavigateBack)
                loadProducts()
            }
            is Resource.Error -> sendEvent(AdminEvent.ShowToast(result.message ?: "Error"))
            else -> Unit
        }
    }

    fun deleteProduct() = viewModelScope.launch {
        val id = currentProduct?.id ?: return@launch
        _isLoading.value = true
        val result = repository.deleteProduct(id)
        _isLoading.value = false

        if (result is Resource.Success) {
            sendEvent(AdminEvent.ShowToast("Deleted"))
            sendEvent(AdminEvent.NavigateBack)
            loadProducts()
        }
    }

    // ==========================================
    //              CATEGORIES LOGIC
    // ==========================================
    private fun loadCategories() = viewModelScope.launch {
        if (repository.getCategories() is Resource.Success) {
            _categories.value = (repository.getCategories() as Resource.Success).data ?: emptyList()
        }
    }

    fun addCategory(name: String) = viewModelScope.launch {
        if (repository.addCategory(Category(id="", name=name)) is Resource.Success) {
            sendEvent(AdminEvent.ShowToast("Category Added"))
            loadCategories()
        }
    }

    fun deleteCategory(id: String) = viewModelScope.launch {
        if (repository.deleteCategory(id) is Resource.Success) loadCategories()
    }

    // ==========================================
    //           ORDERS LOGIC
    // ==========================================
    fun fetchAllOrders() = viewModelScope.launch {
        _allOrders.value = Resource.Loading()
        _allOrders.value = orderRepository.getAllOrders()
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            _updateStatus.value = Resource.Loading()

            try {
                val batch = firestore.batch()

                // 1. Update the Order Status
                val orderRef = firestore.collection("orders").document(order.id)
                batch.update(orderRef, "status", newStatus)

                // 2. Create Notification for User
                val notificationRef = firestore.collection("users").document(order.userId)
                    .collection("notifications").document()

                val notificationData = hashMapOf(
                    "title" to "Order Status Updated",
                    "message" to "Your order #${order.id} is now $newStatus",
                    "date" to System.currentTimeMillis(),
                    "read" to false
                )
                batch.set(notificationRef, notificationData)

                // 3. Restock Logic
                if (newStatus == "Canceled" && order.status != "Canceled") {
                    for (cartItem in order.items) {
                        val productRef = firestore.collection("products").document(cartItem.productId)
                        batch.update(productRef, "amount", FieldValue.increment(cartItem.quantity.toLong()))
                    }
                }

                // 4. Commit all changes
                batch.commit().await()

                _updateStatus.value = Resource.Success(true)
                fetchAllOrders()

            } catch (e: Exception) {
                _updateStatus.value = Resource.Error(e.message ?: "Failed to update")
            }
        }
    }

    // ==========================================
    //              USERS LOGIC
    // ==========================================
    fun fetchAllUsers() = viewModelScope.launch {
        _users.value = Resource.Loading()
        _users.value = repository.getAllUsers()
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        if (repository.deleteUser(user.uid) is Resource.Success) fetchAllUsers()
    }

    fun updateUserRole(uid: String, role: String) = viewModelScope.launch {
        if (repository.updateUserRole(uid, role) is Resource.Success) fetchAllUsers()
    }

    // ==========================================
    //              EVENTS
    // ==========================================
    private fun sendEvent(event: AdminEvent) = viewModelScope.launch {
        _adminEvents.send(event)
    }

    sealed class AdminEvent {
        data class ShowToast(val message: String) : AdminEvent()
        object NavigateBack : AdminEvent()
    }
}