package com.example.ecommerce.ui.theme.fragment

import android.app.AlertDialog // <--- IMPORT THIS
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerce.R
import com.example.ecommerce.adapter.AdminUserAdapter
import com.example.ecommerce.databinding.FragmentAdminUserBinding
import com.example.ecommerce.model.User
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminUserFragment : Fragment(R.layout.fragment_admin_user) {

    private lateinit var binding: FragmentAdminUserBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var userAdapter: AdminUserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminUserBinding.bind(view)

        binding.imgBack.setOnClickListener { findNavController().popBackStack() }
        binding.fabAddUser.setOnClickListener {
            // Option A: Navigate to your existing Signup Fragment
            findNavController().navigate(R.id.action_adminUserFragment_to_signupFragment)
        }
        setupRecyclerView()

        viewModel.fetchAllUsers()
        observeData()
    }

    private fun setupRecyclerView() {
        // We pass TWO functions to the adapter:
        // 1. clicking the row (Edit Role)
        // 2. clicking the trash icon (Delete)
        userAdapter = AdminUserAdapter(
            onUserClick = { user -> showRoleDialog(user) },
            onDeleteClick = { user -> showDeleteDialog(user) },
         onEditClick = { user -> showRoleDialog(user) }
        )

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    // --- LOGIC FOR DELETE CONFIRMATION POP-UP ---
    private fun showDeleteDialog(user: User) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete User?")
        builder.setMessage("Are you sure you want to delete ${user.email}? This action cannot be undone.")

        // "Positive" Button = OK / DELETE
        builder.setPositiveButton("Delete") { dialog, _ ->
            viewModel.deleteUser(user) // <--- Only runs if they click "Delete"
            dialog.dismiss()
        }

        // "Negative" Button = CANCEL
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Just close the pop-up
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    // --- (Optional) Logic for Changing Role ---
    private fun showRoleDialog(user: User) {
        val options = arrayOf("Make ADMIN", "Make USER")
        AlertDialog.Builder(requireContext())
            .setTitle("Change Role for ${user.email}")
            .setItems(options) { _, which ->
                val newRole = if (which == 0) "ADMIN" else "USER"
                viewModel.updateUserRole(user.uid, newRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.users.collectLatest { resource ->
                        when (resource) {
                            // FIX: Use <*> instead of <List<User>>
                            is Resource.Success<*> -> {
                                binding.progressBar.isVisible = false
                                // Cast the data or use the data from the resource safely
                                userAdapter.submitList(resource.data as? List<User> ?: emptyList())
                            }
                            is Resource.Loading<*> -> {
                                binding.progressBar.isVisible = true
                            }
                            is Resource.Error<*> -> {
                                binding.progressBar.isVisible = false
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> Unit
                        }
                    }
                }

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