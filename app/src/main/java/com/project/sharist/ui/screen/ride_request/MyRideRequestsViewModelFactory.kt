package com.project.sharist.ui.screen.ride_request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.sharist.data.repository.UserRepository

class MyRideRequestsViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRideRequestsViewModel::class.java)) {
            return MyRideRequestsViewModel(userRepository = userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
