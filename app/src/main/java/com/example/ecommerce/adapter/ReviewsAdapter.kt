package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemReviewBinding
import com.example.ecommerce.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    // Set this to TRUE in ReviewsFragment, FALSE in ProductDetailFragment
    var isMyReviewsPage: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            // 1. SMART LOGIC: Switch layout based on which screen we are on
            if (isMyReviewsPage) {
                // "My Reviews" Screen: Show Product Name, Hide User Name
                if (review.productName.isNotEmpty()) {
                    binding.tvProductName.text = review.productName
                    binding.tvProductName.visibility = View.VISIBLE
                } else {
                    binding.tvProductName.visibility = View.GONE
                }
                binding.tvUserName.visibility = View.GONE
            } else {
                // "Product Detail" Screen: Hide Product Name, Show User Name
                binding.tvProductName.visibility = View.GONE
                if (review.userName.isNotEmpty()) {
                    binding.tvUserName.text = "by ${review.userName}"
                    binding.tvUserName.visibility = View.VISIBLE
                } else {
                    binding.tvUserName.visibility = View.GONE
                }
            }

            // 2. Common Data
            binding.tvComment.text = review.comment
            binding.ratingBar.rating = review.rating

            // 3. Date Formatting
            val date = Date(review.date)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = formatter.format(date)
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}