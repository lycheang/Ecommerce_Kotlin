package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide  <-- DELETE THIS IMPORT
import com.example.ecommerce.R
import com.example.ecommerce.databinding.ItemProductBinding
import com.example.ecommerce.model.Product
import com.example.ecommerce.util.loadBase64OrUrl // Make sure to import your extension!

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private var onProductClick: ((Product) -> Unit)? = null
    private var onAddToCartClick: ((Product) -> Unit)? = null

    fun setOnProductClickListener(listener: (Product) -> Unit) {
        onProductClick = listener
    }

    fun setOnAddToCartClickListener(listener: (Product) -> Unit) {
        onAddToCartClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "$${product.price}"

            val imgUrl = product.images.firstOrNull()
            binding.imgProduct.loadBase64OrUrl(imgUrl)
            // ---------------------------------------------

            binding.root.setOnClickListener {
                onProductClick?.invoke(product)
            }

            binding.btnAddCart.setOnClickListener {
                onAddToCartClick?.invoke(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}