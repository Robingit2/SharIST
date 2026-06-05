package com.project.sharist.ui.screen.ride_request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.RideRequestRepository
import com.project.sharist.data.repository.UserRepository

class MyRideRequestsViewModelFactory(
    private val rideRequestRepository: RideRequestRepository,
    private val rideOfferRepository: RideOfferRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRideRequestsViewModel::class.java)) {
            return MyRideRequestsViewModel(
                repository = rideRequestRepository,
                rideOfferRepository = rideOfferRepository,
                userRepository = userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
