package com.example.ecommerce.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemCategoryUserBinding // Ensure this matches your XML file name
import com.example.ecommerce.model.Category

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    private var selectedPosition = 0

    inner class CategoryViewHolder(private val binding: ItemCategoryUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, position: Int) {
            binding.tvCategoryName.text = category.name

            Log.d("CategoryAdapter", "Binding category: ${category.name}")

            if (position == selectedPosition) {
                // SELECTED: Purple Background, White Text
                binding.root.setCardBackgroundColor(Color.parseColor("#673AB7"))
                binding.tvCategoryName.setTextColor(Color.WHITE)
            } else {
                // UNSELECTED: Grey Background, Black Text
                binding.root.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                binding.tvCategoryName.setTextColor(Color.BLACK)
            }

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = position
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}