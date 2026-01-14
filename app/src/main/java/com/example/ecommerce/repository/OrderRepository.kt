package com.example.ecommerce.repository

import com.example.ecommerce.model.Order
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // 1. Fetch Orders for the logged-in User (Customer View)
    suspend fun getUserOrders(): Resource<List<Order>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", uid)
                .get().await()

            // Sort by date descending (Newest first)
            val orders = snapshot.toObjects(Order::class.java).sortedByDescending { it.date }
            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch orders")
        }
    }

    // 2. Fetch ALL Orders (Admin View)
//    suspend fun getAllOrders(): Resource<List<Order>> {
//        return try {
//            val snapshot = firestore.collection("orders").get().await()
//            val orders = snapshot.toObjects(Order::class.java).sortedByDescending { it.date }
//            Resource.Success(orders)
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to load all orders")
//        }
//    }

    // 3. Fetch Single Order Details
    suspend fun getOrderById(orderId: String): Resource<Order> {
        return try {
            val document = firestore.collection("orders").document(orderId).get().await()
            val order = document.toObject(Order::class.java)
            if (order != null) {
                Resource.Success(order)
            } else {
                Resource.Error("Order not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch order details")
        }
    }

    // 4. Update Status & Restock Logic (Admin Action)
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Resource<Boolean> {
        return try {
            firestore.runTransaction { transaction ->
                val orderRef = firestore.collection("orders").document(orderId)
                val orderSnapshot = transaction.get(orderRef)

                val currentStatus = orderSnapshot.getString("status") ?: ""
                val rawItems = orderSnapshot.get("items")
                val itemsList = rawItems as? List<Map<String, Any>> ?: emptyList()

                // LOGIC: Only Restock if moving to "Cancelled" and it wasn't already Cancelled
                if (newStatus == "Cancelled" && currentStatus != "Cancelled") {
                    for (itemMap in itemsList) {
                        val productId = (itemMap["productId"] as? String) ?: (itemMap["id"] as? String) ?: ""
                        val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0

                        if (productId.isNotEmpty() && quantity > 0) {
                            val productRef = firestore.collection("products").document(productId)
                            val productSnapshot = transaction.get(productRef)

                            if (productSnapshot.exists()) {
                                val currentStock = productSnapshot.getLong("amount")?.toInt() ?: 0
                                val newStock = currentStock + quantity

                                // Update product document
                                transaction.update(productRef, "amount", newStock)
                                transaction.update(productRef, "inStock", true)
                            }
                        }
                    }
                }

                // Update the actual Order status
                transaction.update(orderRef, "status", newStatus)
            }.await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }

    suspend fun getAllOrders(): Resource<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            val orders = snapshot.toObjects(Order::class.java)
            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch orders")
        }
    }
}