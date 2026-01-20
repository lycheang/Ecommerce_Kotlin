package com.example.ecommerce.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemAdminOrdersBinding
import com.example.ecommerce.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, AdminOrderAdapter.OrderViewHolder>(DiffCallback) {

    inner class OrderViewHolder(private val binding: ItemAdminOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            // 1. Set Text Data
            binding.tvOrderId.text = "Order ID: ${order.id}"
            binding.tvTotalPrice.text = "$${order.totalAmount}"
            binding.tvStatus.text = order.status

            // 2. Format Date
            try {
                val date = Date(order.date)
                val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                binding.tvDate.text = format.format(date)
            } catch (e: Exception) {
                binding.tvDate.text = "Unknown Date"
            }

            // 3. Status Color Logic
            val statusColor = when (order.status) {
                "Pending" -> Color.parseColor("#FFA000") // Orange
                "Confirmed" -> Color.parseColor("#1976D2") // Blue
                "Shipped" -> Color.parseColor("#7B1FA2") // Purple
                "Delivered" -> Color.parseColor("#388E3C") // Green
                "Cancelled" -> Color.RED
                else -> Color.GRAY
            }
            binding.tvStatus.setTextColor(statusColor)

            // 4. Handle Click (Entire Card)
            binding.root.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemAdminOrdersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

}