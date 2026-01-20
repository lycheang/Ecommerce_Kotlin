package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.R
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.databinding.FragmentWishlistBinding
import com.example.ecommerce.ui.theme.viewmodel.WishlistViewModel
import com.example.ecommerce.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment : Fragment(R.layout.fragment_wishlist) {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WishlistViewModel by viewModels()

    // 1. Initialize Adapter
    private val wishlistAdapter by lazy { ProductAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWishlistBinding.bind(view)

        setupRecyclerView()
        setupSwipeToDelete()
        observeWishlist()
    }

    private fun setupRecyclerView() {
        binding.rvWishlist.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = wishlistAdapter
        }

        // Navigate to Product Details
        wishlistAdapter.setOnProductClickListener { product ->
            val action = WishlistFragmentDirections.actionWishlistFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }

        // Add to Cart Logic
        wishlistAdapter.setOnAddToCartClickListener { product ->
            // You can implement "Add to Cart" logic here if needed, or trigger a ViewModel event
            // viewModel.addToCart(product)
            Snackbar.make(requireView(), "Added ${product.name} to cart", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeWishlist() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wishlistProducts.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val products = resource.data ?: emptyList()
                            wishlistAdapter.submitList(products)

                            binding.tvEmptyWishlist.isVisible = products.isEmpty()
                            binding.rvWishlist.isVisible = products.isNotEmpty()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), resource.message ?: "Error", Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val product = wishlistAdapter.currentList[position]

                viewModel.removeFromWishlist(product)

                Snackbar.make(requireView(), "Removed from wishlist", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.addToWishlist(product)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvWishlist)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}