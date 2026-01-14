package com.example.ecommerce.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemNotificationBinding // Make sure you have this layout
import com.example.ecommerce.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.apply {
                tvNotificationTitle.text = notification.title
                tvNotificationMessage.text = notification.message

                val date = Date(notification.date)
                val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                tvNotificationDate.text = format.format(date)

                // Optional: Change background if unread
                if (!notification.read) {
                    root.setCardBackgroundColor(Color.parseColor("#E3F2FD")) // Light Blue
                } else {
                    root.setCardBackgroundColor(Color.WHITE)
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size
}