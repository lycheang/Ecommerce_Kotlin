package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Address
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses = _addresses.asStateFlow()

    private val _addAddressStatus = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val addAddressStatus = _addAddressStatus.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val result = firestore.collection("users").document(uid)
                .collection("addresses").get().await()
            val list = result.toObjects(Address::class.java)
            _addresses.value = list
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun addAddress(address: Address) = viewModelScope.launch {
        _addAddressStatus.value = Resource.Loading()
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("addresses").document() // Generate ID

            val newAddress = address.copy(id = docRef.id)
            docRef.set(newAddress).await()

            _addAddressStatus.value = Resource.Success(true)
            loadAddresses() // Refresh list
        } catch (e: Exception) {
            _addAddressStatus.value = Resource.Error(e.message.toString())
        }
    }

    // Reset status after showing toast
    fun resetAddStatus() {
        _addAddressStatus.value = Resource.Unspecified()
    }
}