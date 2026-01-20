package com.example.ecommerce.ui.theme.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.ReviewsAdapter
import com.example.ecommerce.databinding.FragmentProductDetailsBinding
import com.example.ecommerce.model.Product
import com.example.ecommerce.ui.theme.viewmodel.CartViewModel
import com.example.ecommerce.ui.theme.viewmodel.HomeViewModel
import com.example.ecommerce.ui.theme.viewmodel.ProductDetailViewModel
import com.example.ecommerce.util.Resource
import com.example.ecommerce.util.loadBase64OrUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_details) {

    private lateinit var binding: FragmentProductDetailsBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val detailViewModel: ProductDetailViewModel by viewModels()

    private val args: ProductDetailFragmentArgs by navArgs()
    private val reviewsAdapter = ReviewsAdapter()
    private var currentProduct: Product? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProductDetailsBinding.bind(view)

        // 1. Initial Data Load
        val productId = args.productId
        detailViewModel.loadReviews(productId)
        detailViewModel.checkFavoriteStatus(productId)

        // Load the product details (via HomeViewModel logic or arguments)
        // If your HomeViewModel fetches all products, this triggers the observer below
        // homeViewModel.loadProducts() // Uncomment if needed

        setupReviewsRecycler()
        setupClicks()
        observeViewModels()
    }

    private fun setupReviewsRecycler() {
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
            isNestedScrollingEnabled = false // Keeps scrolling smooth
        }
    }

    private fun setupClicks() {
        // --- 1. Add to Cart Logic ---
        binding.btnAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                cartViewModel.addToCart(product)
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 2. Post Review Logic ---
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.rbNewRating.rating.toInt()
            val comment = binding.etReviewComment.text.toString().trim()

            if (rating < 1) return@setOnClickListener

            currentProduct?.let { product ->
                detailViewModel.addReview(product.id, product.name, rating, comment)
            }
        }

        // --- 3. Favorite Logic ---
        binding.btnFavorite.setOnClickListener {
            detailViewModel.toggleFavorite(args.productId)
        }
    }

    private fun observeViewModels() {
        // --- Observe Product Data ---
        lifecycleScope.launchWhenStarted {
            homeViewModel.products.collectLatest { resource ->
                if (resource is Resource.Success) {
                    currentProduct = resource.data?.find { it.id == args.productId }
                    displayProduct(currentProduct)
                }
            }
        }

        // --- Observe Reviews List (Updates immediately after posting) ---
        lifecycleScope.launchWhenStarted {
            detailViewModel.reviews.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val list = resource.data ?: emptyList()
                        // Update Adapter
                        reviewsAdapter.submitList(list)
                        // Update Header Text
                        binding.tvReviewsHeader.text = "Reviews (${list.size})"
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        // --- Observe Add Review Status (To clear inputs) ---
        lifecycleScope.launchWhenStarted {
            detailViewModel.addReviewStatus.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnSubmitReview.isEnabled = false
                        binding.btnSubmitReview.text = "Posting..."
                    }
                    is Resource.Success -> {
                        binding.btnSubmitReview.isEnabled = true
                        binding.btnSubmitReview.text = "Post Review"

                        // Clear inputs on success
                        binding.etReviewComment.setText("")
                        binding.rbNewRating.rating = 0f

                        Toast.makeText(context, "Review posted!", Toast.LENGTH_SHORT).show()
                        detailViewModel.resetReviewStatus()
                    }
                    is Resource.Error -> {
                        binding.btnSubmitReview.isEnabled = true
                        binding.btnSubmitReview.text = "Post Review"
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        detailViewModel.resetReviewStatus()
                    }
                    else -> Unit
                }
            }
        }

        // --- Observe Wishlist Status ---
        lifecycleScope.launchWhenStarted {
            detailViewModel.isFavorite.collectLatest { isFav ->
                if (isFav) {
                    binding.btnFavorite.setColorFilter(Color.RED)
                } else {
                    binding.btnFavorite.setColorFilter(Color.GRAY)
                }
            }
        }
    }

    private fun displayProduct(product: Product?) {
        product?.let {
            binding.tvProductName.text = it.name
            binding.tvProductPrice.text = "$${it.price}"
            binding.tvProductDesc.text = it.description

            val imgUrl = it.images.firstOrNull()
            binding.imgProductDetail.loadBase64OrUrl(imgUrl)

            if (it.amount > 0) {
                binding.tvStockStatus.text = "In Stock"
                binding.tvStockStatus.setTextColor(Color.parseColor("#4CAF50"))
                binding.btnAddToCart.isEnabled = true
                binding.btnAddToCart.alpha = 1.0f
            } else {
                binding.tvStockStatus.text = "Out of Stock"
                binding.tvStockStatus.setTextColor(Color.RED)
                binding.btnAddToCart.isEnabled = false
                binding.btnAddToCart.alpha = 0.5f
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}