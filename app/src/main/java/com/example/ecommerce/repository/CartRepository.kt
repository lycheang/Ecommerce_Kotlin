package com.example.ecommerce.repository

import com.example.ecommerce.model.Address
import com.example.ecommerce.model.CartItem
import com.example.ecommerce.model.Order
import com.example.ecommerce.model.Product
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // --- 1. GET CART STREAM (Real-time) ---
    // WHY: We use callbackFlow because Firestore listeners are "callback-based",
    // but we want a "Flow" that works nicely with Coroutines.
    suspend fun placeOrder(totalAmount: Double, address: Address, cartItems: List<CartItem>): Resource<String> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        val orderId = firestore.collection("orders").document().id

        return try {
            // --- STEP 1: PRE-CHECK STOCK (Reads Only) ---
            for (item in cartItems) {
                val productSnapshot = firestore.collection("products")
                    .document(item.productId).get().await()

                val currentStock = productSnapshot.getLong("amount") ?: 0
                if (currentStock < item.quantity) {
                    return Resource.Error("Product '${item.name}' is out of stock (Only $currentStock left)")
                }
            }

            // --- STEP 2: EXECUTE WRITES (Batch) ---
            val batch = firestore.batch()

            // A. Save Order
            val orderRef = firestore.collection("orders").document(orderId)
            val order = Order(
                id = orderId,
                userId = uid,
                status = "Pending",
                totalAmount = totalAmount,
                date = System.currentTimeMillis(),
                address = address,
                items = cartItems
            )
            batch.set(orderRef, order)

            // B. Deduct Stock & Delete from Cart
            for (item in cartItems) {
                // 1. Deduct Stock using 'increment(-qty)' (Safe & Atomic)
                val productRef = firestore.collection("products").document(item.productId)
                batch.update(productRef, "amount", FieldValue.increment(-item.quantity.toLong()))

                // 2. Delete from Cart
                // IMPORTANT: Ensure your CartItem model has the correct document ID!
                if (item.id.isNotEmpty()) {
                    val cartRef = firestore.collection("users").document(uid)
                        .collection("cart").document(item.id)
                    batch.delete(cartRef)
                }
            }

            // C. Commit All Changes
            batch.commit().await()

            Resource.Success(orderId)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to place order")
        }
    }
    fun getCartItemsStream(): Flow<Resource<List<CartItem>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        // Listen to the sub-collection "cart"
        val listener: ListenerRegistration = firestore.collection("users").document(uid).collection("cart")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load cart"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    // MANUAL ID FIX: Firestore documents have IDs (e.g., "prod123")
                    for (i in items.indices) {
                        items[i].id = snapshot.documents[i].id
                    }
                    trySend(Resource.Success(items))
                }
            }

        // cleanup when ViewModel stops collecting
        awaitClose { listener.remove() }
    }

    // --- 2. STOCK CHECK (New!) ---
    // WHY: The ViewModel should NOT ask Firestore directly. The Repo must do it.
    suspend fun getProductStock(productId: String): Int {
        return try {
            val snapshot = firestore.collection("products").document(productId).get().await()
            snapshot.getLong("amount")?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // --- 3. ADD / UPDATE / REMOVE ---
    suspend fun addToCart(product: Product): Resource<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val cartRef = firestore.collection("users").document(uid).collection("cart").document(product.id)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(cartRef)
                if (snapshot.exists()) {
                    val currentQty = snapshot.getLong("quantity")?.toInt() ?: 1
                    transaction.update(cartRef, "quantity", currentQty + 1)
                } else {
                    val imgUrl = product.images.firstOrNull() ?: ""
                    val newItem = CartItem(
                        id = product.id,
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        imageUrl = imgUrl,
                        quantity = 1
                    )
                    transaction.set(cartRef, newItem)
                }
            }.await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add")
        }
    }

    suspend fun updateQuantity(cartItemId: String, newQuantity: Int): Resource<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val cartRef = firestore.collection("users").document(uid).collection("cart").document(cartItemId)

            if (newQuantity <= 0) {
                cartRef.delete().await()
            } else {
                cartRef.update("quantity", newQuantity).await()
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update")
        }
    }

    suspend fun removeFromCart(cartItemId: String): Resource<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            firestore.collection("users").document(uid).collection("cart")
                .document(cartItemId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete")
        }
    }

}