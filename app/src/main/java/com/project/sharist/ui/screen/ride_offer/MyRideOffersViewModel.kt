package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.domain.model.RideOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRideOffersUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val offers: List<RideOffer> = emptyList()
)

class MyRideOffersViewModel(
    private val repository: RideOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRideOffersUiState())
    val uiState: StateFlow<MyRideOffersUiState> = _uiState.asStateFlow()

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                _uiState.value = MyRideOffersUiState(
                    offers = repository.getOffers().map { it.toDomain() }
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load ride offers."
                    )
                }
            }
        }
    }

    fun deleteOffer(offer: RideOffer) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                repository.delete(offer.id)
                loadOffers()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not delete ride offer."
                    )
                }
            }
        }
    }
}
