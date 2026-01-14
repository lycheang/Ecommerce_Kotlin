package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.AdminOrderAdapter
import com.example.ecommerce.databinding.FragmentAdminOrdersBinding
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AdminOrdersFragment : Fragment(R.layout.fragment_admin_orders) {

    private lateinit var binding: FragmentAdminOrdersBinding
    private val viewModel: AdminViewModel by viewModels()

    // --- CRITICAL STEP: Handle the Click Here ---
    private val adminOrderAdapter by lazy {
        AdminOrderAdapter { order ->
            // This runs when Admin clicks an order in the list
            val action = AdminOrdersFragmentDirections
                .actionAdminOrdersFragmentToAdminOrdersDetailsFragment(order)
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminOrdersBinding.bind(view)

        setupRecyclerView()

        // Fetch orders when screen opens
        viewModel.fetchAllOrders()

        observeOrders()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.rvAllOrders.apply {
            adapter = adminOrderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeOrders() {
        lifecycleScope.launchWhenStarted {
            viewModel.allOrders.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        binding.progressbarAllOrders.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        // Update the adapter list
                        adminOrderAdapter.submitList(result.data)

                        if (result.data.isNullOrEmpty()) {
                            binding.tvEmptyOrders.visibility = View.VISIBLE
                        } else {
                            binding.tvEmptyOrders.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressbarAllOrders.visibility = View.GONE
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}