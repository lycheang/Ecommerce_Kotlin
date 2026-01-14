package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemBillingProductBinding // Re-using the same XML layout is efficient!
import com.example.ecommerce.model.CartItem
import com.example.ecommerce.util.loadBase64OrUrl

class OrderItemAdapter : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    // This list holds the products. Both Admin and User fragments can set this.
    var differ = emptyList<CartItem>()

    inner class ViewHolder(val binding: ItemBillingProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            binding.tvProductCartName.text = item.name
            binding.tvBillingProductQuantity.text = item.quantity.toString()

            val price = String.format("%.2f", item.price)
            binding.tvProductCartPrice.text = "$ $price"

            // Safe image loading
            binding.imageCartProduct.loadBase64OrUrl(item.imageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBillingProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ[position])
    }

    override fun getItemCount(): Int {
        return differ.size
    }

}