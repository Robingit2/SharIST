package com.project.sharist.ui.screen.available_rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository

class AvailableRidesViewModelFactory(
    private val rideOfferRepository: RideOfferRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AvailableRidesViewModel::class.java)) {
            return AvailableRidesViewModel(
                rideOfferRepository = rideOfferRepository,
                userRepository = userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
