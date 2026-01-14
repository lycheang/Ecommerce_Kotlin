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
import com.example.ecommerce.adapter.CartAdapter
import com.example.ecommerce.databinding.FragmentCartBinding
import com.example.ecommerce.ui.theme.viewmodel.CartViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        setupRecyclerView()
        observeCart()

        binding.btnCheckout.setOnClickListener {
            val currentTotal = viewModel.cartTotal.value
            // Use SafeArgs to pass the float/double value
            val action = CartFragmentDirections.actionCartFragmentToCheckoutFragment(currentTotal.toFloat())
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onPlusClick = { item -> viewModel.increaseQuantity(item) },
            onMinusClick = { item -> viewModel.decreaseQuantity(item) },
            onDeleteClick = { item -> viewModel.removeFromCart(item.id) }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun observeCart() {
        // 1. Observe List of Items
        lifecycleScope.launchWhenStarted {
            viewModel.cartItems.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        cartAdapter.submitList(resource.data)

                        if (resource.data.isNullOrEmpty()) {
                            binding.layoutEmpty.visibility = View.VISIBLE
                            binding.rvCartItems.visibility = View.GONE
                            binding.layoutCheckout.visibility = View.GONE
                        } else {
                            binding.layoutEmpty.visibility = View.GONE
                            binding.rvCartItems.visibility = View.VISIBLE
                            binding.layoutCheckout.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    // FIX: Add this branch to satisfy the compiler
                    else -> Unit
                }
            }
        }

        // 2. Observe Total Price
        lifecycleScope.launchWhenStarted {
            viewModel.cartTotal.collectLatest { total ->
                binding.tvTotalPrice.text = "$${String.format("%.2f", total)}"
            }
        }
    }

}