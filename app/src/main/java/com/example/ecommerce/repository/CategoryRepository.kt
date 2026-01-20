package com.example.ecommerce.repository

import com.example.ecommerce.model.Category
import com.example.ecommerce.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // --- GET ALL CATEGORIES ---
    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            val categories = snapshot.toObjects(Category::class.java)

            Resource.Success(categories)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load categories")
        }
    }

    // --- ADD CATEGORY ---
    suspend fun addCategory(name: String): Resource<Boolean> {
        return try {
            val docRef = firestore.collection("categories").document()

            // FIX: Add 'createdAt' timestamp
            val category = Category(
                id = docRef.id,
                name = name,
                createdAt = System.currentTimeMillis()
            )

            docRef.set(category).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add category")
        }
    }

    // --- DELETE CATEGORY ---
    suspend fun deleteCategory(categoryId: String): Resource<Boolean> {
        return try {
            firestore.collection("categories").document(categoryId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete category")
        }
    }
}