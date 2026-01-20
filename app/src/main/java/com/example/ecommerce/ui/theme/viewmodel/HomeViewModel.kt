package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Category
import com.example.ecommerce.model.Product
import com.example.ecommerce.repository.ProductRepository
import com.example.ecommerce.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ProductRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private var allProductsList = listOf<Product>()

    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val products = _products.asStateFlow()

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Unspecified())
    val categories = _categories.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    // --- SEARCH LOGIC  ---
    fun searchProducts(query: String) {
        // If query is empty, restore the full list we saved earlier
        if (query.isEmpty()) {
            _products.value = Resource.Success(allProductsList)
            return
        }

        // Filter the backup list
        val filteredList = allProductsList.filter { product ->
            product.name.contains(query, ignoreCase = true)
        }

        _products.value = Resource.Success(filteredList)
    }

    // --- LOAD ALL PRODUCTS ---
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading()

            // USE THE NEW FUNCTION HERE
            val result = repo.getActiveProducts()

            if (result is Resource.Success) {
                allProductsList = result.data ?: emptyList()
            }
            _products.value = result
        }
    }

    // --- FILTER BY CATEGORY ---
    fun filterProducts(categoryId: String) {
        if (categoryId == "All") {
            loadProducts()
        } else {
            loadProductsByCategory(categoryId)
        }
    }

    private fun loadProductsByCategory(categoryId: String) {
        viewModelScope.launch {
            _products.emit(Resource.Loading())
            try {
                val snapshot = firestore.collection("products")
                    .whereEqualTo("categoryId", categoryId)
                    .whereEqualTo("inStock", true) // <--- ADD THIS FILTER
                    .get()
                    .await()

                val products = snapshot.toObjects(Product::class.java)
                allProductsList = products
                _products.emit(Resource.Success(products))
            } catch (e: Exception) {
                _products.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.emit(Resource.Loading())
            try {
                val snapshot = firestore.collection("categories").get().await()
                val categoryList = snapshot.toObjects(Category::class.java)
                _categories.emit(Resource.Success(categoryList))
            } catch (e: Exception) {
                _categories.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}