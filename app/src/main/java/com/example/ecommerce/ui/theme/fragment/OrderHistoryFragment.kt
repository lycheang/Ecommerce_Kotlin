package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.OrderAdapter
import com.example.ecommerce.databinding.FragmentOrderHistoryBinding
import com.example.ecommerce.ui.theme.viewmodel.OrderViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryFragment : Fragment(R.layout.fragment_order_history) {

    private lateinit var binding: FragmentOrderHistoryBinding
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrderHistoryBinding.bind(view)

        // Back button logic if you have one in XML
        // binding.imgBack.setOnClickListener { findNavController().popBackStack() }

        setupRecyclerView()
        observeOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { selectedOrder ->
            val action = OrderHistoryFragmentDirections.actionOrderHistoryFragmentToOrderDetailFragment(selectedOrder.id)
            findNavController().navigate(action)
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allOrders.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val list = resource.data ?: emptyList()
                            if (list.isEmpty()) {
                                binding.layoutEmpty.visibility = View.VISIBLE
                                binding.rvOrders.visibility = View.GONE
                            } else {
                                binding.layoutEmpty.visibility = View.GONE
                                binding.rvOrders.visibility = View.VISIBLE
                                orderAdapter.submitList(list)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            // Show toast or error view
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}