package com.project.sharist.ui.screen.ride_offer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.mapper.toDomain
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.domain.model.RideOffer
import com.project.sharist.ui.util.buildRideOfferTitles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRideOffersUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val offers: List<RideOffer> = emptyList(),
    val rideTitles: Map<String, String> = emptyMap()
)

class MyRideOffersViewModel(
    private val repository: RideOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRideOffersUiState())
    val uiState: StateFlow<MyRideOffersUiState> = _uiState.asStateFlow()

    fun loadOffers(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val offers = repository.getOffers().map { it.toDomain() }
                _uiState.value = MyRideOffersUiState(
                    offers = offers,
                    rideTitles = buildRideOfferTitles(context, offers)
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        offers = it.offers.filterNot { existingOffer -> existingOffer.id == offer.id },
                        rideTitles = it.rideTitles - offer.id
                    )
                }
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
