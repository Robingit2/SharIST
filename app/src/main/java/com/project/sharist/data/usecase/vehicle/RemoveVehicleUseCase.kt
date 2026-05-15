package com.project.sharist.data.usecase.vehicle

import com.project.sharist.data.repository.VehicleRepository

class RemoveVehicleUseCase (
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: String) {
        vehicleRepository.delete(vehicleId)
    }
}