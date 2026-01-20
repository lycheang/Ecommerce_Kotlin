package com.example.ecommerce.ui.theme.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentAdminProductBinding
import com.example.ecommerce.model.Category
import com.example.ecommerce.model.Product
import com.example.ecommerce.ui.theme.viewmodel.AdminViewModel
import com.example.ecommerce.util.loadBase64OrUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminProductFragment : Fragment(R.layout.fragment_admin_product) {

    private lateinit var binding: FragmentAdminProductBinding
    private val viewModel: AdminViewModel by viewModels()
    private val args: AdminProductFragmentArgs by navArgs()
    private var categoriesList: List<Category> = emptyList()

    // Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.selectedImageUri = it
            binding.imgPreview.setImageURI(it) // Show the new image immediately
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminProductBinding.bind(view)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Back Button
        binding.imgBack.setOnClickListener { findNavController().popBackStack() }

        val product = args.product
        viewModel.currentProduct = product

        if (product != null) {
            // --- EDIT MODE ---
            binding.tvTitle.text = "Update Product"
            binding.btnSave.text = "Update Product"
            binding.btnDelete.visibility = View.VISIBLE

            // Pre-fill fields
            binding.etName.setText(product.name)
            binding.etPrice.setText(product.price.toString())
            binding.etDescription.setText(product.description)
            binding.etAmount.setText(product.amount.toString())
            binding.switchStock.isChecked = product.inStock

            // --- LOAD IMAGE (The Clean Way) ---
            val imgString = product.images.firstOrNull()
            binding.imgPreview.loadBase64OrUrl(imgString)

        } else {
            // --- ADD MODE ---
            binding.tvTitle.text = "Create Product"
            binding.btnSave.text = "Create Product"
            binding.btnDelete.visibility = View.GONE
            binding.switchStock.isChecked = true
            binding.etAmount.setText("0")
        }
    }

    private fun setupListeners() {
        // 1. Pick Image
        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }

        // 2. Save / Update
        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        // 3. Delete
        binding.btnDelete.setOnClickListener {
            viewModel.deleteProduct()
        }
    }

    private fun saveProduct() {
        val name = binding.etName.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        // 1. READ THE SWITCH VALUE
        val isSwitchOn = binding.switchStock.isChecked

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(context, "Name and Price are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoriesList.isEmpty()) {
            Toast.makeText(context, "Please create a category first", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryIdx = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categoryIdx >= 0) categoriesList[categoryIdx].id else ""

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val amount = amountStr.toIntOrNull() ?: 0

        val product = Product(
            id = viewModel.currentProduct?.id ?: "",
            name = name,
            description = desc,
            price = price,
            amount = amount,

            // 2. PASS THE SWITCH VALUE (Not just "amount > 0")
            // We pass what the user *wants*. The Repository will double-check
            // that stock isn't 0.
            inStock = isSwitchOn,

            categoryId = categoryId,
            images = viewModel.currentProduct?.images ?: emptyList()
        )

        viewModel.saveProduct(product)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Observe Events
                launch {
                    viewModel.adminEvents.collect { event ->
                        when (event) {
                            is AdminViewModel.AdminEvent.ShowToast -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }
                            is AdminViewModel.AdminEvent.NavigateBack -> {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }

                // 2. Observe Loading
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.isVisible = isLoading
                        binding.btnSave.isEnabled = !isLoading
                        binding.btnDelete.isEnabled = !isLoading
                        binding.btnPickImage.isEnabled = !isLoading
                    }
                }

                // 3. Observe Categories
                launch {
                    viewModel.categories.collectLatest { categories ->
                        categoriesList = categories
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categories.map { it.name }
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerCategory.adapter = adapter

                        // Restore selection if editing
                        if (viewModel.currentProduct != null) {
                            val index = categories.indexOfFirst { it.id == viewModel.currentProduct!!.categoryId }
                            if (index != -1) binding.spinnerCategory.setSelection(index)
                        }
                    }
                }
            }
        }
    }
}