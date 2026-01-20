package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide  <-- REMOVED
import com.example.ecommerce.databinding.ItemCartBinding
import com.example.ecommerce.model.CartItem
import com.example.ecommerce.util.loadBase64OrUrl // Import your helper

class CartAdapter(
    private val onPlusClick: (CartItem) -> Unit,
    private val onMinusClick: (CartItem) -> Unit,
    private val onDeleteClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvProductName.text = item.name
            binding.tvProductPrice.text = "$${item.price}"
            binding.tvQuantity.text = item.quantity.toString()

            // --- FIX: USE HELPER ONLY ---
            binding.imgProduct.loadBase64OrUrl(item.imageUrl)
            // ----------------------------

            binding.btnPlus.setOnClickListener { onPlusClick(item) }
            binding.btnMinus.setOnClickListener { onMinusClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}