package com.example.ecommerce.ui.theme.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.OrderItemAdapter
import com.example.ecommerce.databinding.FragmentAdminOrdersDetailsBinding
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AdminOrdersDetailsFragment : Fragment(R.layout.fragment_admin_orders_details) {

    private lateinit var binding: FragmentAdminOrdersDetailsBinding
    private val viewModel: AdminViewModel by viewModels()
    private val args: AdminOrdersDetailsFragmentArgs by navArgs()
    private val productsAdapter by lazy { OrderItemAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminOrdersDetailsBinding.bind(view)

        setupUI()
        setupRecyclerView()
        setupStatusSpinner()
        observeViewModel()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateOrder.setOnClickListener {
            updateOrderStatus()
        }
    }

    private fun setupUI() {
        val order = args.order

        binding.apply {
            tvOrderId.text = "Order ID: #${order.id}"
            tvOrderStatus.text = "Status: ${order.status}"

            // --- PRICE BREAKDOWN ---
            tvSubtotal.text = "$${String.format("%.2f", order.subtotal)}"

            if (order.deliveryFee > 0) {
                tvDeliveryFee.text = "$${String.format("%.2f", order.deliveryFee)}"
                tvDeliveryFee.setTextColor(Color.BLACK)
            } else {
                tvDeliveryFee.text = "Free"
                tvDeliveryFee.setTextColor(Color.parseColor("#4CAF50")) // Green
            }

            if (order.discountAmount > 0) {
                layoutDiscount.visibility = View.VISIBLE
                tvDiscount.text = "-$${String.format("%.2f", order.discountAmount)}"
            } else {
                layoutDiscount.visibility = View.GONE
            }

            tvTotalPrice.text = "$${String.format("%.2f", order.totalAmount)}"

            // --- INFO ---
            tvFullName.text = order.address.fullName
            tvAddress.text = order.address.addressLine
            tvPhoneNumber.text = order.address.phoneNumber

            val date = Date(order.date)
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            tvOrderDate.text = "Date: ${format.format(date)}"

            // Set Initial Color
            setStatusColor(order.status)
        }

        productsAdapter.differ = order.items
        productsAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        binding.rvOrderItems.apply {
            adapter = productsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupStatusSpinner() {
        val statusList = listOf("Pending", "Confirmed", "Shipped", "Delivered", "Canceled")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter

        val currentStatus = args.order.status
        val position = statusList.indexOf(currentStatus)
        if (position >= 0) {
            binding.spinnerStatus.setSelection(position)
        }
    }

    private fun updateOrderStatus() {
        val newStatus = binding.spinnerStatus.selectedItem.toString()
        // FIX: Use 'args.order' directly. 'currentOrder' was undefined.
        viewModel.updateOrderStatus(args.order, newStatus)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateStatus.collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // 1. SHOW LOADING
                            binding.btnUpdateOrder.text = "Saving..."
                            binding.btnUpdateOrder.isEnabled = false // Disable button
                            binding.progressBarUpdate.visibility = View.VISIBLE // Show spinner
                        }
                        is Resource.Success -> {
                            // 2. SHOW SUCCESS
                            binding.progressBarUpdate.visibility = View.INVISIBLE
                            binding.btnUpdateOrder.text = "Save Status"
                            binding.btnUpdateOrder.isEnabled = true

                            // Update UI text immediately
                            val newStatus = binding.spinnerStatus.selectedItem.toString()
                            binding.tvOrderStatus.text = "Status: $newStatus"

                            // Change color if you have that function
                            setStatusColor(newStatus)

                            Toast.makeText(context, "Status Updated!", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Error -> {
                            // 3. SHOW ERROR
                            binding.progressBarUpdate.visibility = View.INVISIBLE
                            binding.btnUpdateOrder.text = "Save Status"
                            binding.btnUpdateOrder.isEnabled = true

                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    // Helper to keep color logic in one place
    private fun setStatusColor(status: String) {
        when (status) {
            "Canceled", "Cancelled" -> binding.tvOrderStatus.setTextColor(Color.RED)
            "Delivered" -> binding.tvOrderStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            "Pending", "Ordered" -> binding.tvOrderStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
            else -> binding.tvOrderStatus.setTextColor(Color.BLACK)
        }
    }
}