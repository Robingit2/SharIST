package com.project.sharist.data.usecase.vehicle

import android.content.Context
import android.net.Uri
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.user.AddVehicleInput
import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.data.repository.VehicleRepository
import java.util.UUID

class AddVehicleUseCase (
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(context: Context, input: AddVehicleInput): GenericResult<Unit> {
        val vehicleId = UUID.randomUUID().toString()
        val photoPath = when (val result = input.photoPath?.trim()?.takeIf { it.isNotEmpty() }?.let { path ->
            if (path.startsWith("content://")) {
                vehicleRepository.uploadVehiclePhoto(
                    context = context,
                    userId = input.userId,
                    vehicleId = vehicleId,
                    uri = Uri.parse(path)
                )
            } else {
                GenericResult.Success(path)
            }
        }) {
            is GenericResult.Success -> result.data
            is GenericResult.Error -> return result
            null -> null
        }

        return vehicleRepository.insert(Vehicle(
            id = vehicleId,
            plate = input.plate,
            photoPath = photoPath,
            userId = input.userId
        ))
    }
}
