package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.RideOfferRepository

class MyRideOffersViewModelFactory(
    private val repository: RideOfferRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRideOffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRideOffersViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
