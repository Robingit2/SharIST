package com.project.sharist.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(

        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        var userRole: String,
        var vehicleNumber: String,
        var vehicleModel: String,
        var email: String,
        var address: String,
        var fullName: String,
        var phone: String,
        var document: String,
        var password: String
)