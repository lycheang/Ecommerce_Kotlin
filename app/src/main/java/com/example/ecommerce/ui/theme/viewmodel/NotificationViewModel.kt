package com.example.ecommerce.ui.theme.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Notification
import com.example.ecommerce.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    application: Application
) : AndroidViewModel(application) {

    private val _notifications = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading())
    val notifications = _notifications.asStateFlow()

    init {
        getNotifications()
    }

    fun getNotifications() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _notifications.value = Resource.Loading()

            // Listen to real-time updates
            firestore.collection("users").document(uid).collection("notifications")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _notifications.value = Resource.Error(error.message.toString())
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val list = snapshot.toObjects(Notification::class.java)
                        _notifications.value = Resource.Success(list)
                    }
                }
        }
    }
}