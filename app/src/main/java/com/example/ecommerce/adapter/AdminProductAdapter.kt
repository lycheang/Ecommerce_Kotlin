package com.example.ecommerce.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide <-- DELETE THIS
import com.example.ecommerce.databinding.ItemAdminProductBinding
import com.example.ecommerce.model.Product
import com.example.ecommerce.util.loadBase64OrUrl // Import your helper

class AdminProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, AdminProductAdapter.ProductViewHolder>(DiffCallback) {

    inner class ProductViewHolder(private val binding: ItemAdminProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "$${product.price}"

            // --- STOCK ALERT LOGIC ---
            val stock = product.amount

            when {
                stock == 0 -> {
                    binding.tvStockStatus.text = "OUT OF STOCK (0)"
                    binding.tvStockStatus.setTextColor(Color.RED)
                }
                stock < 5 -> {
                    binding.tvStockStatus.text = "LOW STOCK ($stock)"
                    binding.tvStockStatus.setTextColor(Color.parseColor("#FFA500")) // Orange
                }
                else -> {
                    binding.tvStockStatus.text = "In Stock ($stock)"
                    binding.tvStockStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                }
            }

            // --- FIX: USE YOUR HELPER HERE TOO ---
            val imgUrl = product.images.firstOrNull()
            binding.imgProduct.loadBase64OrUrl(imgUrl)
            // -------------------------------------

            binding.root.setOnClickListener { onProductClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemAdminProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}