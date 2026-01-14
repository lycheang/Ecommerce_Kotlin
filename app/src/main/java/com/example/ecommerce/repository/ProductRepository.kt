package com.example.ecommerce.repository

import android.content.Context
import android.util.Base64
import com.example.ecommerce.model.Category
import com.example.ecommerce.model.Order
import com.example.ecommerce.model.Product
import com.example.ecommerce.model.User
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    // ---------- ADMIN: SAVE PRODUCT ----------
    // FIX: We accept 'ByteArray?' directly. No more Uri processing here.
    suspend fun saveProduct(product: Product, imageBytes: ByteArray?): Resource<Boolean> {
        return try {
            val ref = if (product.id.isEmpty()) firestore.collection("products").document()
            else firestore.collection("products").document(product.id)

            var finalImages = product.images

            // IF we have new image data (from ViewModel)
            if (imageBytes != null) {
                val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                if (imageString != null) {
                    finalImages = listOf(imageString)
                } else {
                    return Resource.Error("Failed to encode image")
                }
            }

            // --- THE LOGIC YOU WANTED ---
            // 1. If Stock is 0: Force Inactive (Safety check).
            // 2. If Stock > 0: Respect the Admin Switch (product.inStock).
            //    This allows you to turn it OFF even if you have 100 items!
            val finalInStock = if (product.amount > 0) {
                product.inStock // Use the value from the Admin Switch
            } else {
                false // No stock = Always hidden
            }

            val finalProduct = product.copy(
                id = ref.id,
                images = finalImages,
                inStock = finalInStock // <--- Uses your manual setting
            )

            ref.set(finalProduct).await()
            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to save product")
        }
    }

    // ---------- STANDARD FUNCTIONS ----------

    suspend fun getProducts(): Resource<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.toObjects(Product::class.java)
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load products")
        }
    }

    // 2. Add this for HOME/USER (Fetches only Active items)
    suspend fun getActiveProducts(): Resource<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("inStock", true) // <--- THE FILTER
                .get().await()
            val products = snapshot.toObjects(Product::class.java)
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load active products")
        }
    }

    suspend fun deleteProduct(productId: String): Resource<Boolean> {
        return try {
            firestore.collection("products").document(productId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete")
        }
    }

    // ---------- CATEGORIES ----------
    suspend fun getCategories(): Resource<List<Category>> {
        val snapshot = firestore.collection("categories").get().await()
        return Resource.Success(snapshot.toObjects(Category::class.java))
    }

    suspend fun addCategory(category: Category): Resource<Boolean> {
        val ref = firestore.collection("categories").document()
        ref.set(category.copy(id = ref.id)).await()
        return Resource.Success(true)
    }

    suspend fun deleteCategory(categoryId: String): Resource<Boolean> {
        firestore.collection("categories").document(categoryId).delete().await()
        return Resource.Success(true)
    }

    // ---------- USERS ----------
    suspend fun getAllUsers(): Resource<List<User>> {
        val snapshot = firestore.collection("users").get().await()
        return Resource.Success(snapshot.toObjects(User::class.java))
    }

    suspend fun updateUserRole(uid: String, role: String): Resource<Boolean> {
        firestore.collection("users").document(uid).update("role", role).await()
        return Resource.Success(true)
    }

    suspend fun deleteUser(uid: String): Resource<Boolean> {
        firestore.collection("users").document(uid).delete().await()
        return Resource.Success(true)
    }

    // ---------- WISHLIST ----------
    suspend fun getWishlistProducts(): Resource<List<Product>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Login required")
            val wishlistDocs = firestore.collection("users").document(uid)
                .collection("wishlist").get().await()

            val ids = wishlistDocs.documents.map { it.id }
            if (ids.isEmpty()) return Resource.Success(emptyList())

            val products = ids.chunked(10).flatMap { chunk ->
                firestore.collection("products")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get().await()
                    .toObjects(Product::class.java)
            }
            Resource.Success(products)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load wishlist")
        }
    }

    suspend fun toggleWishlist(productId: String, isAdd: Boolean): Resource<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Login required")
            val ref = firestore.collection("users").document(uid).collection("wishlist").document(productId)

            if (isAdd) {
                ref.set(mapOf("productId" to productId, "addedAt" to System.currentTimeMillis())).await()
            } else {
                ref.delete().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Action failed")
        }
    }

    suspend fun removeFromWishlist(productId: String): Resource<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Login required")
            firestore.collection("users").document(uid)
                .collection("wishlist").document(productId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove")
        }
    }

    // ---------- UPDATE ORDER ----------
    suspend fun updateOrderStatus(order: Order, newStatus: String): Resource<Boolean> {
        return try {
            val batch = firestore.batch()
            val orderRef = firestore.collection("orders").document(order.id)

            // 1. Update the Order Status
            batch.update(orderRef, "status", newStatus)

            // 2. If Canceled, Restock items
            if (newStatus == "Canceled" || newStatus == "Cancelled") {
                order.items.forEach { cartItem ->
                    val productRef = firestore.collection("products").document(cartItem.productId)
                    // Use 'increment' to add stock back atomically
                    batch.update(productRef, "amount", FieldValue.increment(cartItem.quantity.toLong()))
                }
            }

            batch.commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }
}