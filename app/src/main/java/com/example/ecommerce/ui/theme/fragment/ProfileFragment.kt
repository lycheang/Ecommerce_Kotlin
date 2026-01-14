package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    // 1. Inject Firestore for Admin Check
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupUserInfo()
        setupClickListeners()
    }

    private fun setupUserInfo() {
        val user = auth.currentUser

        // 1. Get the name
        val name = user?.displayName

        // 2. Check if it is empty
        if (name.isNullOrEmpty()) {
            // FIX: Show this text if Firebase has no name
            binding.tvName.text = "Guest User"
            binding.tvName.setTextColor(android.graphics.Color.RED) // Debug color
        } else {
            // Show the real name
            binding.tvName.text = name
            binding.tvName.setTextColor(android.graphics.Color.BLACK)
        }

        binding.tvEmail.text = user?.email ?: "No Email"
    }

    private fun setupClickListeners() {
        // Orders
        binding.btnOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_orderHistoryFragment)
        }

        // Edit Profile (Fixed)
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Address (Fixed)
        binding.btnAddress.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addressFragment)
        }

        // Payment Methods (Fixed)
        binding.btnPaymentMethods.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_paymentMethodsFragment)
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }


}