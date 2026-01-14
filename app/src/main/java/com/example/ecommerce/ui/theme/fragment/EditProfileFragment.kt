package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding

    // Inject dependencies if you want to use Firestore
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)

        // 1. Load current data
        val user = auth.currentUser
        binding.etName.setText(user?.displayName)
        binding.etEmail.setText(user?.email)
        // Note: Phone number usually comes from Firestore, not Auth, unless they signed up with phone.
        // binding.etPhone.setText(user?.phoneNumber)

        // 2. Save Logic
        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            val newPassword = binding.etPassword.text.toString().trim()
            val newPhone = binding.etPhone.text.toString().trim()

            if (newName.isEmpty()) {
                binding.etName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // Start the update process
            binding.btnSave.isEnabled = false
            binding.btnSave.text = "Saving..."

            updateUser(newName, newPassword, newPhone)
        }
    }

    private fun updateUser(name: String, password: String, phone: String) {
        val user = auth.currentUser
        if (user == null) return

        // 1. Update Display Name
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
            if (profileTask.isSuccessful) {

                // 2. Update Password (if provided)
                if (password.isNotEmpty()) {
                    user.updatePassword(password).addOnCompleteListener { passwordTask ->
                        if (passwordTask.isSuccessful) {
                            // Password updated successfully
                            handlePhoneUpdate(phone)
                        } else {
                            // Password failed (Likely requires re-authentication)
                            showError("Password Update Failed: ${passwordTask.exception?.message}")
                        }
                    }
                } else {
                    // No password change requested, move to phone
                    handlePhoneUpdate(phone)
                }

            } else {
                showError("Profile Update Failed: ${profileTask.exception?.message}")
            }
        }
    }

    // 3. Save Phone to Firestore (Optional but Recommended)
    private fun handlePhoneUpdate(phone: String) {
        if (phone.isNotEmpty()) {
            val userId = auth.currentUser?.uid ?: return
            val userMap = hashMapOf("phone" to phone)

            firestore.collection("users").document(userId)
                .set(userMap, com.google.firebase.firestore.SetOptions.merge()) // Merge so we don't delete other data
                .addOnSuccessListener {
                    showSuccess()
                }
                .addOnFailureListener {
                    showSuccess() // Even if firestore fails, Auth succeeded, so we can still close
                    // Or show error: showError(it.message)
                }
        } else {
            showSuccess()
        }
    }

    private fun showSuccess() {
        Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun showError(message: String?) {
        binding.btnSave.isEnabled = true
        binding.btnSave.text = "Save Changes"
        Toast.makeText(context, message ?: "Unknown Error", Toast.LENGTH_LONG).show()
    }
}