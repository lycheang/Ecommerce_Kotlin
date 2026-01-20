package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.User
import com.example.ecommerce.repository.AuthRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val loginState = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val signupState = _signupState.asStateFlow()

    private val _updatePassword = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val updatePassword = _updatePassword.asStateFlow()
    private val _resetPassword = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val resetPassword = _resetPassword.asStateFlow()

    private val _otpState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val otpState = _otpState.asStateFlow()
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
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.value = Resource.Loading()
            _resetPassword.value = repo.sendPasswordResetEmail(email)
        }
    }
    fun changePassword(newPass: String, confirmPass: String) {
        if (newPass != confirmPass) {
            viewModelScope.launch {
                _updatePassword.emit(Resource.Error("Passwords do not match"))
            }
            return
        }

        if (newPass.length < 6) {
            viewModelScope.launch {
                _updatePassword.emit(Resource.Error("Password must be 6+ chars"))
            }
            return
        }

        viewModelScope.launch {
            _updatePassword.emit(Resource.Loading())
            _updatePassword.emit(repo.updatePassword(newPass))
        }
    }
    fun sendOtp(email: String) {
        viewModelScope.launch {
            _otpState.value = Resource.Loading()
            delay(1000) // Fake loading time
            // In a real app, you call API here. For Demo, we just say Success.
            _otpState.value = Resource.Success("1234") // We hardcode the OTP for demo
        }
    }

    // 2. Mock Reset Password
    fun mockResetPassword() {
        viewModelScope.launch {
            delay(1000)
            // We cannot actually reset Firebase password without the old one or a link.
            // So we just return Success for the presentation demo.
            _otpState.value = Resource.Success("Password Reset Successfully")
        }
    }
}