package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private lateinit var binding: FragmentAdminDashboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminDashboardBinding.bind(view)

        // 1. Go to Manage Products
        binding.cardProducts.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_productList)
        }

        // 2. Go to Manage Categories
        binding.cardCategory.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_category)
        }

        // 3. Go to Manage Users
        binding.cardUsers.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_users)
        }

        // 4. Go to Orders (Report)
        binding.cardOrders.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_orders)
        }

        // Logout
        binding.imgLogout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_login)
        }
        
    }
}