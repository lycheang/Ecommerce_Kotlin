package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Address
import com.example.ecommerce.model.CartItem
import com.example.ecommerce.model.Order
import com.example.ecommerce.model.PaymentCard
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // --- STATES ---
    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress = _selectedAddress.asStateFlow()

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses = _addresses.asStateFlow()

    private val _cards = MutableStateFlow<List<PaymentCard>>(emptyList())
    val cards = _cards.asStateFlow()

    // 1. Restore Selected Card State
    private val _selectedCard = MutableStateFlow<PaymentCard?>(null)
    val selectedCard = _selectedCard.asStateFlow()

    private val _orderState = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val orderState = _orderState.asStateFlow()

    init {
        fetchAddresses()
        fetchCards()
    }

    // --- FETCH DATA ---
    private fun fetchAddresses() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val snapshot = firestore.collection("users").document(uid).collection("addresses").get().await()
            val list = snapshot.toObjects(Address::class.java)
            _addresses.value = list

            // Auto-select default address
            if (_selectedAddress.value == null) {
                _selectedAddress.value = list.find { it.isDefault } ?: list.firstOrNull()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun fetchCards() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val snapshot = firestore.collection("users").document(uid).collection("cards").get().await()
            _cards.value = snapshot.toObjects(PaymentCard::class.java)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // --- SELECTION ---
    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun selectCard(card: PaymentCard) {
        _selectedCard.value = card
    }

    // --- ADD DATA ---
    fun addNewAddress(address: Address) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val docRef = firestore.collection("users").document(uid).collection("addresses").document()
            val newAddress = address.copy(id = docRef.id)
            docRef.set(newAddress).await()
            fetchAddresses()
            _selectedAddress.value = newAddress
        } catch (e: Exception) { e.printStackTrace() }
    }

    // --- PLACE ORDER (FIXED LOGIC) ---
    // Note: We only accept 'paymentMethod'. Total is calculated here for safety.
    // --- PLACE ORDER (FIXED & SAFE) ---
    fun placeOrder(paymentMethod: String) = viewModelScope.launch {
        _orderState.value = Resource.Loading()

        val uid = auth.currentUser?.uid ?: return@launch
        val address = _selectedAddress.value

        if (address == null) {
            _orderState.value = Resource.Error("Please select an address")
            return@launch
        }

        try {
            // 1. Get Cart Items
            val cartSnapshot = firestore.collection("users").document(uid).collection("cart").get().await()
            val rawCartItems = cartSnapshot.toObjects(CartItem::class.java)

            if (rawCartItems.isEmpty()) {
                _orderState.value = Resource.Error("Cart is empty")
                return@launch
            }

            // 2. DATA SANITIZATION (Keep this! It's good)
            val finalCartItems = rawCartItems.mapIndexed { index, item ->
                val realId = if (item.productId.isNotEmpty()) item.productId else if (item.id.isNotEmpty()) item.id else cartSnapshot.documents[index].id
                item.copy(id = realId, productId = realId)
            }

            // 3. CALCULATE TOTALS
            var subtotal = 0.0
            for (item in finalCartItems) subtotal += (item.price * item.quantity)

            val discountPercentage = when {
                subtotal > 500 -> 0.50
                subtotal >= 200 -> 0.30
                subtotal >= 100 -> 0.20
                subtotal > 50 -> 0.10
                else -> 0.0
            }
            val discountAmount = subtotal * discountPercentage
            val deliveryFee = if (subtotal > 50) 0.0 else 3.0
            val finalTotal = (subtotal - discountAmount) + deliveryFee

            // 4. ATOMIC TRANSACTION (Split into READ phase and WRITE phase)
            firestore.runTransaction { transaction ->

                // --- PHASE A: READS ONLY ---
                // We create a list to hold the product data we just read
                val productUpdates = mutableListOf<Pair<String, Long>>() // Pair(ProductId, NewStockAmount)

                for (item in finalCartItems) {
                    val productRef = firestore.collection("products").document(item.productId)
                    val productSnapshot = transaction.get(productRef) // READ

                    if (!productSnapshot.exists()) {
                        throw Exception("Product '${item.name}' not found")
                    }

                    val currentStock = productSnapshot.getLong("amount") ?: 0

                    // CHECK STOCK
                    if (currentStock < item.quantity) {
                        throw Exception("Not enough stock for ${item.name}. Only $currentStock left.")
                    }

                    // Calculate new stock but DO NOT WRITE YET
                    val newStock = currentStock - item.quantity
                    productUpdates.add(Pair(item.productId, newStock))
                }

                // --- PHASE B: WRITES ONLY ---
                // Now that all reads are finished successfully, we can write.

                // 1. Update Products
                for ((productId, newStock) in productUpdates) {
                    val productRef = firestore.collection("products").document(productId)
                    transaction.update(productRef, "amount", newStock)

                    if (newStock == 0L) {
                        transaction.update(productRef, "inStock", false)
                    }
                }

                // 2. Create Order
                val orderRef = firestore.collection("orders").document()
                val newOrder = Order(
                    id = orderRef.id,
                    userId = uid,
                    items = finalCartItems,
                    address = address,
                    paymentMethod = paymentMethod,
                    subtotal = subtotal,
                    discountAmount = discountAmount,
                    deliveryFee = deliveryFee,
                    totalAmount = finalTotal,
                    status = "Ordered",
                    date = System.currentTimeMillis()
                )
                transaction.set(orderRef, newOrder)

                // 3. Clear Cart
                for (doc in cartSnapshot.documents) {
                    transaction.delete(doc.reference)
                }

            }.await()

            _orderState.value = Resource.Success(true)

        } catch (e: Exception) {
            _orderState.value = Resource.Error(e.message ?: "Failed to place order")
        }
    }


    fun resetOrderState() {
        _orderState.value = Resource.Unspecified()
    }
}
