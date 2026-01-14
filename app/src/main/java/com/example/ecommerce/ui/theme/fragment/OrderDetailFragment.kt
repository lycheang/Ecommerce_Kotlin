package com.example.ecommerce.ui.theme.fragment

import android.graphics.Color // <--- ADDED THIS IMPORT
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.OrderItemAdapter
import com.example.ecommerce.databinding.FragmentOrderDetailsBinding
import com.example.ecommerce.ui.theme.viewmodel.OrderViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OrderDetailFragment : Fragment(R.layout.fragment_order_details) {

    private lateinit var binding: FragmentOrderDetailsBinding
    private val viewModel: OrderViewModel by viewModels()
    private val args: OrderDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrderDetailsBinding.bind(view)

        // 1. Get ID and fetch details
        val orderId = args.orderId
        viewModel.getOrderDetails(orderId)

//        binding.btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }

        observeDetails()
    }

    private fun observeDetails() {
        lifecycleScope.launchWhenStarted {
            viewModel.orderDetails.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE

                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val order = resource.data
                        if (order != null) {
                            // --- 1. Basic Info ---
                            binding.tvOrderId.text = "Order #${order.id.take(8).uppercase()}"
                            binding.tvOrderStatus.text = order.status

                            val date = Date(order.date)
                            val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            binding.tvOrderDate.text = "Placed on ${format.format(date)}"

                            // Address
                            binding.tvAddress.text = "${order.address.fullName}, ${order.address.addressLine}\n${order.address.phoneNumber}"

                            // --- 2. Cost Breakdown ---

                            // Subtotal
                            binding.tvSubtotal.text = "$${String.format("%.2f", order.subtotal)}"

                            // Discount
                            if (order.discountAmount > 0) {
                                binding.tvDiscount.text = "-$${String.format("%.2f", order.discountAmount)}"
                                binding.tvDiscount.visibility = View.VISIBLE
                            } else {
                                binding.tvDiscount.text = "$0.00"
                                // Optional: Hide the text view if 0
                                // binding.tvDiscount.visibility = View.GONE
                            }

                            // Delivery Fee
                            if (order.deliveryFee == 0.0) {
                                binding.tvDeliveryFee.text = "Free"
                                binding.tvDeliveryFee.setTextColor(Color.parseColor("#388E3C")) // Green
                            } else {
                                binding.tvDeliveryFee.text = "$${String.format("%.2f", order.deliveryFee)}"
                                binding.tvDeliveryFee.setTextColor(Color.BLACK)
                            }

                            // Final Total
                            binding.tvTotalAmount.text = "$${String.format("%.2f", order.totalAmount)}"

                            // --- 3. Items List ---
                            val itemAdapter = OrderItemAdapter()
                            itemAdapter.differ = order.items  // Set data manually
                            binding.rvOrderItems.adapter = itemAdapter
                        }
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}