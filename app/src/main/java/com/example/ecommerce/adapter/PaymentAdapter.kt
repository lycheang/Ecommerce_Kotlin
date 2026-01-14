package com.example.ecommerce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerce.databinding.ItemPaymentMethodBinding
import com.example.ecommerce.model.PaymentCard

class PaymentAdapter(
    private val onCardSelected: (PaymentCard) -> Unit
) : ListAdapter<PaymentCard, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    // Track selected item index
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PaymentViewHolder(private val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: PaymentCard, position: Int) {
            // Mask the card number for security (show only last 4)
            val maskedNumber = if (card.cardNumber.length >= 4)
                "**** **** **** ${card.cardNumber.takeLast(4)}"
            else card.cardNumber

            binding.tvCardNumber.text = maskedNumber
            binding.tvHolderName.text = card.cardHolder
            binding.tvExpiry.text = "Exp: ${card.expiryDate}"

            // Check RadioButton if this position is selected OR card is default
            binding.rbSelected.isChecked = (position == selectedPosition)

            // Click Logic
            binding.root.setOnClickListener { handleSelection() }
            binding.rbSelected.setOnClickListener { handleSelection() }
        }

        private fun handleSelection() {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val previousPos = selectedPosition
                selectedPosition = pos

                notifyItemChanged(previousPos) // Update old row
                notifyItemChanged(selectedPosition) // Update new row

                onCardSelected(getItem(pos))
            }
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<PaymentCard>() {
        override fun areItemsTheSame(oldItem: PaymentCard, newItem: PaymentCard) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PaymentCard, newItem: PaymentCard) = oldItem == newItem
    }
}