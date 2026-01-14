package com.example.ecommerce.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemUserBinding
import com.example.ecommerce.model.User

class AdminUserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit, // <--- NEW PARAMETER
    private val onEditClick: (User) -> Unit
) : ListAdapter<User, AdminUserAdapter.UserViewHolder>(DiffCallback) {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvEmail.text = user.email
            binding.tvUid.text = "UID: ${user.uid}"
            binding.tvRole.text = user.role

            // Role Styling
            if (user.role == "ADMIN") {
                binding.tvRole.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                binding.tvRole.setTextColor(Color.WHITE)
            } else {
                binding.tvRole.setBackgroundColor(Color.parseColor("#E0E0E0")) // Gray
                binding.tvRole.setTextColor(Color.BLACK)
            }

            // 1. Click on Row -> Change Role
            binding.root.setOnClickListener {
                onUserClick(user)
            }

            // 2. Click on Trash Icon -> Delete
            binding.btnDelete.setOnClickListener {
                onDeleteClick(user)
            }
            binding.btnEdit.setOnClickListener {
                onEditClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }

}