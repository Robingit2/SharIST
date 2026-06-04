package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class RideRequestRepository {

    private val rideRequestsTable = supabase.postgrest["ride_requests"]

    suspend fun insert(request: RideRequestEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            rideRequestsTable.insert(request)
        }
    }

    suspend fun getFutureRequestsByPassenger(passengerId: String, after: String): GenericResult<List<RideRequestEntity>> {
        return getFutureRequestsByPassenger(passengerId, after, from = 0, to = DEFAULT_PAGE_SIZE - 1)
    }

    suspend fun getFutureRequestsByPassenger(
        passengerId: String,
        after: String,
        from: Long,
        to: Long
    ): GenericResult<List<RideRequestEntity>> {
        return safeSupabaseCall {
            rideRequestsTable.select {
                filter {
                    eq("passenger_id", passengerId)
                    gte("departure_time", after)
                }
                order("departure_time", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    suspend fun delete(requestId: String): GenericResult<Unit> {
        return safeSupabaseCall {
            rideRequestsTable.delete {
                filter {
                    eq("id", requestId)
                }
            }
        }
    }
}

private const val DEFAULT_PAGE_SIZE = 10L
