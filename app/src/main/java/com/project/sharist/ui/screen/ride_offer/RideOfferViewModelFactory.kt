package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.usecase.ride.InsertRideOfferUseCase

class RideOfferViewModelFactory(
    private val insertRideOfferUseCase: InsertRideOfferUseCase,
    private val rideOfferRepository: RideOfferRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RideOfferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RideOfferViewModel(insertRideOfferUseCase, rideOfferRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
