package com.example.ecommerce.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.model.Review
import com.example.ecommerce.repository.CartRepository
import com.example.ecommerce.repository.ReviewRepository
import com.example.ecommerce.repository.WishlistRepository
import com.example.ecommerce.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<Resource<List<Review>>>(Resource.Loading())
    val reviews = _reviews.asStateFlow()

    private val _addReviewStatus = MutableStateFlow<Resource<Boolean>?>(null)
    val addReviewStatus = _addReviewStatus.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun loadReviews(productId: String) {
        viewModelScope.launch {
            _reviews.value = reviewRepository.getReviews(productId)
        }
    }

    fun addReview(productId: String, productName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _addReviewStatus.value = Resource.Loading()
            val result = reviewRepository.addReview(productId, productName, rating, comment)
            _addReviewStatus.value = result

            if (result is Resource.Success) {
                loadReviews(productId)
            }
        }
    }

    fun resetReviewStatus() {
        _addReviewStatus.value = null
    }

    fun checkFavoriteStatus(productId: String) {
        viewModelScope.launch {
            val result = wishlistRepository.getWishlist()
            if (result is Resource.Success) {
                val exists = result.data?.any { it.productId == productId } == true
                _isFavorite.value = exists
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                wishlistRepository.removeFromWishlist(productId)
                _isFavorite.value = false
            } else {
                wishlistRepository.addToWishlist(productId)
                _isFavorite.value = true
            }
        }
    }
}