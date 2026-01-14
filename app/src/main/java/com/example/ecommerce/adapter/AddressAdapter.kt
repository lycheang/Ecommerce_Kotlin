package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemAddressBinding
import com.example.ecommerce.model.Address

class AddressAdapter(
    private val onAddressSelected: (Address) -> Unit
) : ListAdapter<Address, AddressAdapter.AddressViewHolder>(AddressDiffCallback()) {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class AddressViewHolder(private val binding: ItemAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address, position: Int) {
            binding.tvLabel.text = address.fullName
            binding.tvAddressDetails.text = address.addressLine
            binding.tvPhone.text = address.phoneNumber

            // Radio Button State
            binding.rbSelected.isChecked = (position == selectedPosition) || address.isDefault

            // --- CLICK LISTENER FIX ---
            binding.root.setOnClickListener {
                handleItemClick()
            }
            binding.rbSelected.setOnClickListener {
                handleItemClick()
            }
        }

        private fun handleItemClick() {
            // 1. Get the current live position
            val pos = bindingAdapterPosition

            // 2. Safety Check: Ensure position is valid
            if (pos != RecyclerView.NO_POSITION) {
                updateSelection(pos)
                // 3. Get the ACTUAL item at this position (Safe)
                onAddressSelected(getItem(pos))
            }
        }
    }

    private fun updateSelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    class AddressDiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Address, newItem: Address) = oldItem == newItem
    }
}