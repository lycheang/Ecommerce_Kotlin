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
import com.example.ecommerce.adapter.AddressAdapter
import com.example.ecommerce.databinding.FragmentAddressBinding
import com.example.ecommerce.model.Address
import com.example.ecommerce.ui.theme.viewmodel.AddressViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddressFragment : Fragment(R.layout.fragment_address) {

    private lateinit var binding: FragmentAddressBinding
    private val viewModel: AddressViewModel by viewModels()
    private lateinit var addressAdapter: AddressAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddressBinding.bind(view)

        setupRecyclerView()
        observeData()

        binding.btnAddAddress.setOnClickListener {
            showAddAddressDialog()
        }
    }

    private fun setupRecyclerView() {
        addressAdapter = AddressAdapter { selectedAddress ->
            // 1. Create a bundle with the selected address
            val result = Bundle().apply {
                putParcelable("selected_address", selectedAddress)
            }

            // 2. Send the result to CheckoutFragment
            parentFragmentManager.setFragmentResult("address_request", result)

            // 3. Go back to the previous screen (Checkout)
            findNavController().popBackStack()
        }

        binding.rvAddresses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addressAdapter
        }
    }

    private fun observeData() {
        // Observe List
        lifecycleScope.launchWhenStarted {
            viewModel.addresses.collectLatest { list ->
                addressAdapter.submitList(list)
            }
        }

        // Observe Add Status
        lifecycleScope.launchWhenStarted {
            viewModel.addAddressStatus.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Address Added Successfully", Toast.LENGTH_SHORT).show()
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

    private fun showAddAddressDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_address, null)
        builder.setView(dialogView)
        builder.setTitle("Add New Address")

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        builder.setPositiveButton("Save") { _, _ ->
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
                val newAddress = Address(
                    fullName = name, // Using 'fullName' as label (e.g., Home) for simplicity
                    addressLine = address,
                    phoneNumber = phone,
                    isDefault = false
                )
                viewModel.addAddress(newAddress)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

}