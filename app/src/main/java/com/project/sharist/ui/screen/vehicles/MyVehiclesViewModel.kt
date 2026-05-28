package com.project.sharist.ui.screen.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.user.AddVehicleInput
import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.data.repository.VehicleRepository
import com.project.sharist.data.usecase.vehicle.AddVehicleUseCase
import com.project.sharist.data.usecase.vehicle.RemoveVehicleUseCase
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyVehiclesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val vehicles: List<Vehicle> = emptyList()
)

class MyVehiclesViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val addVehicleUseCase: AddVehicleUseCase = AddVehicleUseCase(vehicleRepository),
    private val removeVehicleUseCase: RemoveVehicleUseCase = RemoveVehicleUseCase(vehicleRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyVehiclesUiState())
    val uiState: StateFlow<MyVehiclesUiState> = _uiState.asStateFlow()

    fun loadVehicles() {
        val userId = supabase.auth.currentUserOrNull()?.id

        if (userId == null) {
            _uiState.value = MyVehiclesUiState(errorMessage = "No logged in user found.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                _uiState.value = MyVehiclesUiState(
                    vehicles = vehicleRepository.getVehiclesByUser(userId)
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not load vehicles."
                    )
                }
            }
        }
    }

    fun addVehicle(plate: String, photoPath: String?) {
        val userId = supabase.auth.currentUserOrNull()?.id

        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "No logged in user found.") }
            return
        }

        if (plate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Plate is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                addVehicleUseCase(
                    AddVehicleInput(
                        plate = plate.trim(),
                        photoPath = photoPath?.trim()?.takeIf { it.isNotEmpty() },
                        userId = userId
                    )
                )
                loadVehicles()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not add vehicle."
                    )
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                removeVehicleUseCase(vehicleId)
                loadVehicles()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Could not delete vehicle."
                    )
                }
            }
        }
    }
}
