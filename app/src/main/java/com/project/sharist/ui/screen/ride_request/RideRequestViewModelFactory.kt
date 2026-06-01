package com.project.sharist.ui.screen.ride_request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.usecase.ride.InsertRideRequestUseCase

class RideRequestViewModelFactory(
    private val insertRideRequestUseCase: InsertRideRequestUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RideRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RideRequestViewModel(insertRideRequestUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}
