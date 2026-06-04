package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository

class MyRideOffersViewModelFactory(
    private val repository: RideOfferRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRideOffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRideOffersViewModel(
                repository = repository,
                userRepository = userRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
