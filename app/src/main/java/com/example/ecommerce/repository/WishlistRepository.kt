package com.example.ecommerce.repository

import com.example.ecommerce.model.WishList
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun addToWishlist(productId: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")

        return try {
            val wishlistItem = WishList(id = productId, productId = productId)
            firestore.collection("users").document(uid)
                .collection("wishlist").document(productId)
                .set(wishlistItem).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add to wishlist")
        }
    }

    suspend fun removeFromWishlist(productId: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        return try {
            firestore.collection("users").document(uid)
                .collection("wishlist").document(productId)
                .delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove from wishlist")
        }
    }

    suspend fun getWishlist(): Resource<List<WishList>> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("wishlist").get().await()
            val items = snapshot.toObjects(WishList::class.java)
            Resource.Success(items)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load wishlist")
        }
    }

}