package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.PaymentCard
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
class PaymentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _cards = MutableStateFlow<List<PaymentCard>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _addCardStatus = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val addCardStatus = _addCardStatus.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val result = firestore.collection("users").document(uid)
                .collection("cards").get().await()
            val list = result.toObjects(PaymentCard::class.java)
            _cards.value = list
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    fun addCard(card: PaymentCard) = viewModelScope.launch {
        _addCardStatus.value = Resource.Loading()
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val docRef = firestore.collection("users").document(uid)
                .collection("cards").document()

            val newCard = card.copy(id = docRef.id)
            docRef.set(newCard).await()

            _addCardStatus.value = Resource.Success(true)
            loadCards() // Refresh list
        } catch (e: Exception) {
            _addCardStatus.value = Resource.Error(e.message ?: "Failed to add card")
        }
    }

    fun resetAddStatus() {
        _addCardStatus.value = Resource.Unspecified()
    }
}