package com.project.sharist.data.repository

import com.project.sharist.data.model.user.Vehicle
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class VehicleRepository {

    val vehiclesTable = supabase.postgrest["vehicles"]

    suspend fun getVehicleById(vehicleId: String) : Vehicle {
        return vehiclesTable.select {
            filter {
                eq("id", vehicleId)
            }
        }.decodeSingle()
    }

    suspend fun getVehiclesByUser(userId: String) : List<Vehicle> {
        return vehiclesTable.select {
            filter {
                eq("user_id", userId)
            }
        }.decodeSingle()
    }

    suspend fun insert(vehicle: Vehicle) {
        vehiclesTable.upsert(vehicle)
    }

    suspend fun update(vehicleId: String, updates: Map<String, Any>) {
        vehiclesTable.update(updates) {
            filter {
                eq("vehicle_id", vehicleId)
            }
        }
    }

    suspend fun delete(vehicleId: String) {
        vehiclesTable.delete {
            filter {
                eq("id", vehicleId)
            }
        }
    }
}