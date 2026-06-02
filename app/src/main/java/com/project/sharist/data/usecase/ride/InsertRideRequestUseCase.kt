package com.project.sharist.data.usecase.ride

import com.project.sharist.data.repository.RideRequestRepository
import com.project.sharist.data.mapper.toEntity
import com.project.sharist.domain.model.RideRequest


class InsertRideRequestUseCase(
    private val rideRequestRepository: RideRequestRepository
) {
    suspend operator fun invoke(request: RideRequest) {
        rideRequestRepository.insert(request.toEntity())
    }
}
