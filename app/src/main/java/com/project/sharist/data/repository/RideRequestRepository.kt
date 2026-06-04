package com.project.sharist.data.repository

import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class RideRequestRepository {

    private val rideRequestsTable = supabase.postgrest["ride_requests"]

    suspend fun insert(request: RideRequestEntity) {
        rideRequestsTable.insert(request)
    }

    suspend fun getFutureRequestsByPassenger(passengerId: String, after: String): List<RideRequestEntity> {
        return rideRequestsTable.select {
            filter {
                eq("passenger_id", passengerId)
                gte("departure_time", after)
            }
        }.decodeList()
    }

    suspend fun delete(requestId: String) {
        rideRequestsTable.delete {
            filter {
                eq("id", requestId)
            }
        }
    }
}
