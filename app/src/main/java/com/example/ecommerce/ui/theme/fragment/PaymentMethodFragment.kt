package com.example.ecommerce.ui.theme.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.PaymentAdapter
import com.example.ecommerce.databinding.FragmentPaymentMethodBinding
import com.example.ecommerce.model.PaymentCard
import com.example.ecommerce.ui.theme.viewmodel.PaymentViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PaymentMethodFragment : Fragment(R.layout.fragment_payment_method) {

    private lateinit var binding: FragmentPaymentMethodBinding
    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPaymentMethodBinding.bind(view)

        setupRecyclerView()
        observeData()

        binding.btnAddCard.setOnClickListener {
            showAddCardDialog()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter { selectedCard ->
            // 1. Package the selected card
            val result = Bundle().apply {
                putParcelable("selected_card", selectedCard)
            }

            // 2. Send result to CheckoutFragment
            parentFragmentManager.setFragmentResult("payment_request", result)

            // 3. Close this screen and go back
            findNavController().popBackStack()
        }

        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentAdapter
        }
    }

    private fun observeData() {
        // Observe Card List
        lifecycleScope.launchWhenStarted {
            viewModel.cards.collectLatest { list ->
                paymentAdapter.submitList(list)
            }
        }

        // Observe Add Status
        lifecycleScope.launchWhenStarted {
            viewModel.addCardStatus.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Card Added Successfully", Toast.LENGTH_SHORT).show()
                        viewModel.resetAddStatus()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetAddStatus()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showAddCardDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_card, null)
        builder.setView(dialogView)
        builder.setTitle("Add New Card")

        val etNumber = dialogView.findViewById<EditText>(R.id.etCardNumber)
        val etHolder = dialogView.findViewById<EditText>(R.id.etHolderName)
        val etExpiry = dialogView.findViewById<EditText>(R.id.etExpiryDate)

        builder.setPositiveButton("Save") { _, _ ->
            val number = etNumber.text.toString().trim()
            val holder = etHolder.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()

            if (number.length >= 16 && holder.isNotEmpty() && expiry.isNotEmpty()) {
                val newCard = PaymentCard(
                    cardNumber = number,
                    cardHolder = holder,
                    expiryDate = expiry
                )
                viewModel.addCard(newCard)
            } else {
                Toast.makeText(context, "Invalid Details. Check card number length.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}