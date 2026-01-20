package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.AdminProductAdapter
import com.example.ecommerce.databinding.FragmentAdminProductListBinding
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminProductListFragment : Fragment(R.layout.fragment_admin_product_list) {

    private var _binding: FragmentAdminProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels()

    // 1. Initialize Adapter
    private val productAdapter by lazy {
        AdminProductAdapter { product ->
            // Navigate to Edit Mode (Pass the product)
            val action = AdminProductListFragmentDirections.actionProductListToProductEditor(product)
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminProductListBinding.bind(view)

        // Setup UI
        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
        setupRecyclerView()

        // 2. FAB Click -> Navigate to Create Mode (Pass null)
        binding.fabAddProduct.setOnClickListener {
            val action = AdminProductListFragmentDirections.actionProductListToProductEditor(null)
            findNavController().navigate(action)
        }

        // 3. Trigger Data Load
        // We call this every time to ensure the list is fresh after editing/adding
        viewModel.loadProducts()

        observeData()
    }

    private fun setupRecyclerView() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Observe Products List
                launch {
                    viewModel.products.collectLatest { productList ->
                        productAdapter.submitList(productList)

                    }
                }

                // 2. Observe Loading State (Separate from data)
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }

                // 3. Observe Error Events (Toasts)
                launch {
                    viewModel.adminEvents.collect { event ->
                        if (event is AdminViewModel.AdminEvent.ShowToast) {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}