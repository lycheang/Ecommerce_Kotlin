package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.ReviewsAdapter
import com.example.ecommerce.databinding.FragmentReviewsBinding
import com.example.ecommerce.ui.theme.viewmodel.ReviewsViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ReviewsFragment : Fragment(R.layout.fragment_reviews) {

    private lateinit var binding: FragmentReviewsBinding
    private val reviewsAdapter = ReviewsAdapter()
    private val viewModel: ReviewsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewsBinding.bind(view)

        setupRecyclerView()
        observeReviews()
    }

    private fun setupRecyclerView() {
        // IMPORTANT: Tell adapter we want to see Product Names
        reviewsAdapter.isMyReviewsPage = true

        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
        }
    }

    private fun observeReviews() {
        lifecycleScope.launchWhenStarted {
            viewModel.userReviews.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.tvEmpty.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        val list = resource.data ?: emptyList()
                        if (list.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                            binding.rvReviews.visibility = View.GONE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                            binding.rvReviews.visibility = View.VISIBLE
                            reviewsAdapter.submitList(list)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}