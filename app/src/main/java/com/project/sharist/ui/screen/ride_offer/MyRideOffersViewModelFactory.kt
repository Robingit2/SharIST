package com.project.sharist.ui.screen.ride_offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.ReservationRepository
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository

class MyRideOffersViewModelFactory(
    private val repository: RideOfferRepository,
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRideOffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRideOffersViewModel(
                repository = repository,
                reservationRepository = reservationRepository,
                userRepository = userRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
