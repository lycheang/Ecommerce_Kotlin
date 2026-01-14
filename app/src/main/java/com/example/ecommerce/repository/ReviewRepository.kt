package com.example.ecommerce.repository

import com.example.ecommerce.model.Review
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    // 1. Add Review (Saves Product Name now)
    suspend fun addReview(productId: String, productName: String, rating: Int, comment: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        val userName = auth.currentUser?.displayName ?: "Anonymous"

        val review = Review(
            userId = uid,
            userName = userName,
            productId = productId,
            productName = productName,
            rating = rating.toFloat(),
            comment = comment,
            date = System.currentTimeMillis()
        )

        return try {
            val reviewRef = firestore.collection("products").document(productId)
                .collection("reviews").document()
            review.id = reviewRef.id
            reviewRef.set(review).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add review")
        }
    }

    // 2. Get Reviews for a SPECIFIC PRODUCT (Product Detail Screen)
    suspend fun getReviews(productId: String): Resource<List<Review>> {
        return try {
            val snapshot = firestore.collection("products").document(productId)
                .collection("reviews")
                .orderBy("date", Query.Direction.DESCENDING)
                .get().await()
            val reviews = snapshot.toObjects(Review::class.java)
            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load reviews")
        }
    }

    // 3. Get Reviews for CURRENT USER (My Reviews Screen)
    suspend fun getUserReviews(): Resource<List<Review>> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")

        return try {
            // NOTE: Requires "Collection Group" Index in Firestore Console
            val snapshot = firestore.collectionGroup("reviews")
                .whereEqualTo("userId", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val reviews = snapshot.toObjects(Review::class.java)
            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed. Check Logcat for Index Link.")
        }
    }
}