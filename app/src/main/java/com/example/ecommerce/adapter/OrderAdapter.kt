package com.example.ecommerce.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemOrderBinding
import com.example.ecommerce.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val onOrderClicked: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvOrderId.text = "Order #${order.id.take(8).uppercase()}"

            // Format Date
            val date = Date(order.date)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = formatter.format(date)

            binding.tvOrderStatus.text = order.status
            // Format Currency
            binding.tvTotalAmount.text = "$${String.format("%.2f", order.totalAmount)}"

            // Status Colors
            val statusColor = when (order.status) {
                "Delivered" -> Color.parseColor("#388E3C") // Green
                "Cancelled" -> Color.parseColor("#D32F2F") // Red
                "Ordered" -> Color.parseColor("#2196F3")   // Blue
                else -> Color.parseColor("#F57C00")        // Orange (Processing)
            }
            binding.tvOrderStatus.setTextColor(statusColor)

            binding.root.setOnClickListener {
                onOrderClicked(order)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
    }
}