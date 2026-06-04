package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.RideMatchEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class RideMatchRepository {

    private val rideMatchesTable = supabase.postgrest["ride_matches"]

    suspend fun getMatchesByRequest(requestId: String): GenericResult<List<RideMatchEntity>> {
        return getMatchesByRequest(requestId, from = 0, to = DEFAULT_PAGE_SIZE - 1)
    }

    suspend fun getMatchesByRequest(requestId: String, from: Long, to: Long): GenericResult<List<RideMatchEntity>> {
        return safeSupabaseCall {
            rideMatchesTable.select {
                filter {
                    eq("ride_request_id", requestId)
                }
                order("ride_offer_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    suspend fun getMatchesByOffer(offerId: String): GenericResult<List<RideMatchEntity>> {
        return safeSupabaseCall {
            rideMatchesTable.select {
                filter {
                    eq("ride_offer_id", offerId)
                }
            }.decodeList()
        }
    }
}

private const val DEFAULT_PAGE_SIZE = 10L
