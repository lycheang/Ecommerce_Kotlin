package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.AdminCategoryAdapter
import com.example.ecommerce.databinding.FragmentAdminCategoryBinding
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCategoryFragment : Fragment(R.layout.fragment_admin_category) {

    private lateinit var binding: FragmentAdminCategoryBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var categoryAdapter: AdminCategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminCategoryBinding.bind(view)

        // 1. Back Button
        binding.imgBack.setOnClickListener { findNavController().popBackStack() }

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. Add Category Button Logic
        binding.btnAddCat.setOnClickListener {
            val name = binding.etNewCategory.text.toString().trim()

            if (name.isNotEmpty()) {
                viewModel.addCategory(name)

                // Clear input
                binding.etNewCategory.text?.clear()
                binding.etNewCategory.clearFocus()
            } else {
                Toast.makeText(context, "Enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Observe Data
        observeData()
    }

    private fun setupRecyclerView() {
        categoryAdapter = AdminCategoryAdapter { category ->
            // DELETE: Pass the ID to the ViewModel
            viewModel.deleteCategory(category.id)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Categories
                launch {
                    viewModel.categories.collectLatest { list ->
                        categoryAdapter.submitList(list)
                    }
                }

                // Observe Events
                launch {
                    viewModel.adminEvents.collect { event ->
                        if (event is AdminViewModel.AdminEvent.ShowToast) {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}