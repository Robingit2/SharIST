package com.project.sharist.data.usecase.vehicle

import com.project.sharist.data.model.user.AddVehicleInput
import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.data.repository.VehicleRepository

class AddVehicleUseCase (
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(input: AddVehicleInput) {
        vehicleRepository.insert(Vehicle(
            plate = input.plate,
            photoPath = input.photoPath,
            userId = input.userId
        ))
    }
}