package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentCheckoutBinding
import com.example.ecommerce.model.Address
import com.example.ecommerce.model.PaymentCard
import com.example.ecommerce.ui.theme.viewmodel.CheckoutViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var binding: FragmentCheckoutBinding
    private val viewModel: CheckoutViewModel by viewModels()
    private val args: CheckoutFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckoutBinding.bind(view)

        val totalFromCart = args.totalAmount
        binding.tvTotalAmount.text = "$${String.format("%.2f", totalFromCart)}"
        setupClickListeners()
        setupPaymentMethodToggle()
        setupFragmentResultListeners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.layoutAddressSelector.setOnClickListener {
            findNavController().navigate(R.id.action_checkoutFragment_to_addressFragment)
        }

        binding.layoutCardSelector.setOnClickListener {
            findNavController().navigate(R.id.action_checkoutFragment_to_paymentMethodsFragment)
        }

        binding.btnPlaceOrder.setOnClickListener {
            handlePlaceOrder()
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener("address_request", viewLifecycleOwner) { _, bundle ->
            val address = bundle.getParcelable<Address>("selected_address")
            if (address != null) viewModel.selectAddress(address)
        }

        parentFragmentManager.setFragmentResultListener("payment_request", viewLifecycleOwner) { _, bundle ->
            val card = bundle.getParcelable<PaymentCard>("selected_card")
            if (card != null) viewModel.selectCard(card)
        }
    }

    private fun observeData() {
        // 1. Watch Address
        lifecycleScope.launchWhenStarted {
            viewModel.selectedAddress.collectLatest { address ->
                if (address != null) {
                    binding.tvAddressName.text = address.fullName
                    binding.tvAddressPhone.text = "${address.addressLine}\n${address.phoneNumber}"
                } else {
                    binding.tvAddressName.text = "Select Delivery Address"
                    binding.tvAddressPhone.text = "Tap to choose address"
                }
            }
        }

        // 2. Watch Card
        lifecycleScope.launchWhenStarted {
            viewModel.selectedCard.collectLatest { card ->
                if (card != null) {
                    val masked = "**** **** **** ${card.cardNumber.takeLast(4)}"
                    binding.tvCardInfo.text = "$masked (${card.cardHolder})"
                } else {
                    binding.tvCardInfo.text = "Select Card"
                }
            }
        }

        // 3. Watch Order Status (Consolidated & Fixed)
        lifecycleScope.launchWhenStarted {
            viewModel.orderState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnPlaceOrder.text = "Processing..."
                        binding.btnPlaceOrder.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.btnPlaceOrder.text = "Order Placed!"
                        binding.btnPlaceOrder.isEnabled = true

                        Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()

                        // CRITICAL FIX: Check if we are still on the Checkout screen before navigating
                        if (findNavController().currentDestination?.id == R.id.checkoutFragment) {
                            findNavController().navigate(R.id.action_checkoutFragment_to_orderHistoryFragment)
                        }

                        // Reset state to prevent re-triggering navigation on back press
                        viewModel.resetOrderState()
                    }
                    is Resource.Error -> {
                        binding.btnPlaceOrder.text = "Place Order"
                        binding.btnPlaceOrder.isEnabled = true
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupPaymentMethodToggle() {
        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCreditCard) {
                binding.cardPaymentSelector.visibility = View.VISIBLE
            } else {
                binding.cardPaymentSelector.visibility = View.GONE
            }
        }
    }

    private fun handlePlaceOrder() {
        val paymentMethod = if (binding.rbCash.isChecked) "Cash on Delivery" else "Credit Card"
        viewModel.placeOrder(paymentMethod)
    }

}