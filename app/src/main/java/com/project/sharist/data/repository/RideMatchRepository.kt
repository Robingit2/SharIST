package com.project.sharist.data.repository

import com.project.sharist.data.model.ride.RideMatchEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class RideMatchRepository {

    private val rideMatchesTable = supabase.postgrest["ride_matches"]

    suspend fun getMatchesByRequest(requestId: String): List<RideMatchEntity> {
        return rideMatchesTable.select {
            filter {
                eq("ride_request_id", requestId)
            }
        }.decodeList()
    }

    suspend fun getMatchesByOffer(offerId: String): List<RideMatchEntity> {
        return rideMatchesTable.select {
            filter {
                eq("ride_offer_id", offerId)
            }
        }.decodeList()
    }
}
