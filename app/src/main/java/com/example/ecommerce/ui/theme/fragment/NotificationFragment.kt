package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.NotificationAdapter
import com.example.ecommerce.databinding.FragmentNotificationBinding
import com.example.ecommerce.ui.theme.viewmodel.NotificationViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private lateinit var binding: FragmentNotificationBinding
    private val notificationAdapter by lazy { NotificationAdapter() }
    private val viewModel: NotificationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationBinding.bind(view)

        setupRecyclerView()
        observeNotifications()

        // Back button logic (if you have one)
        binding.imageBack?.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar?.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar?.visibility = View.GONE
                            val list = resource.data ?: emptyList()

                            notificationAdapter.differ.submitList(list)

                            if (list.isEmpty()) {
                                binding.layoutEmpty.visibility = View.VISIBLE // Show the whole empty group
                                binding.rvNotifications.visibility = View.GONE
                            } else {
                                binding.layoutEmpty.visibility = View.GONE
                                binding.rvNotifications.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar?.visibility = View.GONE
                        }
                        else -> Unit
                    }

                }

            }
        }
    }
}