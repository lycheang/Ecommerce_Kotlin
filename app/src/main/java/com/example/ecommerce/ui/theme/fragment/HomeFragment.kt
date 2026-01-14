package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.CategoryAdapter
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.databinding.FragmentHomeBinding
import com.example.ecommerce.model.Category
import com.example.ecommerce.ui.theme.viewmodel.CartViewModel
import com.example.ecommerce.ui.theme.viewmodel.HomeViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val productAdapter by lazy { ProductAdapter() }
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupRecyclerViews()
        setupSearch() // <--- Updated function
        observeData()
    }

    // ... (setupRecyclerViews stays the same) ...

    private fun setupSearch() {
        // IMPROVEMENT: Use TextWatcher for "As-You-Type" searching
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pass the text to ViewModel immediately
                val query = s.toString().trim()
                homeViewModel.searchProducts(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ... (observeData stays the same) ...

    private fun setupRecyclerViews() {
        // --- 1. PRODUCT ADAPTER (Grid) ---
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // Navigate to Details
        productAdapter.setOnProductClickListener { product ->
            val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }

        // Add to Cart
        productAdapter.setOnAddToCartClickListener { product ->
            if (product.amount > 0) {
                cartViewModel.addToCart(product)
                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Out of Stock!", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 2. CATEGORY ADAPTER (Horizontal) ---
        categoryAdapter = CategoryAdapter { selectedCategory ->
            if (selectedCategory.id == "0" || selectedCategory.name == "All") {
                homeViewModel.filterProducts("All")
            } else {
                homeViewModel.filterProducts(selectedCategory.id)
            }
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.products.collectLatest { resource ->
                when(resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvProducts.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val productList = resource.data ?: emptyList()

                        if (productList.isEmpty()) {
                            binding.rvProducts.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.rvProducts.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                            productAdapter.submitList(productList)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            homeViewModel.categories.collectLatest { resource ->
                if (resource is Resource.Success) {
                    val firestoreList = resource.data ?: emptyList()
                    val fullList = ArrayList<Category>()
                    fullList.add(Category("0", "All", ""))
                    fullList.addAll(firestoreList)
                    categoryAdapter.submitList(fullList)
                }
            }
        }
    }
}