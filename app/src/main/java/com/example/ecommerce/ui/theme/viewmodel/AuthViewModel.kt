package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.User
import com.example.ecommerce.repository.AuthRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {

    // FIX: Change Resource<Any> to Resource<User> so we can access user.role
    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val signupState = _signupState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            // The Repository already fetches the User object from Firestore
            val result = repo.login(email, pass)
            _loginState.value = result
        }
    }

    fun signup(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            val result = repo.signup(name, email, pass)
            _signupState.value = result
        }
    }
}