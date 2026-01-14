package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Review
import com.example.ecommerce.repository.ReviewRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _userReviews = MutableStateFlow<Resource<List<Review>>>(Resource.Loading())
    val userReviews = _userReviews.asStateFlow()

    init {
        loadUserReviews()
    }

    fun loadUserReviews() {
        viewModelScope.launch {
            _userReviews.value = Resource.Loading()
            _userReviews.value = reviewRepository.getUserReviews()
        }
    }
}