package com.project.sharist.data.model.user

data class AddVehicleInput (
    val plate: String,
    val photoPath: String? = null,
    val userId: String
)