package com.example.ecommerce.repository

import com.example.ecommerce.model.User
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest // Import this!
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // ... Login function stays the same ...
    suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Resource.Error("Authentication failed")

            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)

            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Success(User(uid = uid, email = email, role = "USER"))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun signup(name: String, email: String, pass: String): Resource<Boolean> {
        return try {
            // 1. Create User
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: return Resource.Error("Failed to create user")
            val uid = firebaseUser.uid

            // 2. CRITICAL FIX: Update the Firebase Auth Profile with the Name
            // This ensures auth.currentUser.displayName is NOT null
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Save to Firestore (Database)
            val newUser = User(uid = uid, name = name, email = email, role = "USER")
            firestore.collection("users").document(uid).set(newUser).await()

            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Signup failed")
        }
    }
    suspend fun sendPasswordResetEmail(email: String): Resource<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success("Reset link sent to your email")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset email")
        }
    }
    suspend fun updatePassword(newPass: String): Resource<Boolean> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.updatePassword(newPass).await() // 👈 This updates it instantly!
                Resource.Success(true)
            } else {
                Resource.Error("User not logged in")
            }
        } catch (e: Exception) {
            // NOTE: If the user logged in a long time ago, Firebase might ask them
            // to re-login before allowing this. (Security Sensitive Action)
            Resource.Error(e.message ?: "Update failed. Please logout and login again.")
        }
    }
}