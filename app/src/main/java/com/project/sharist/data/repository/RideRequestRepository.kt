package com.project.sharist.data.repository

import com.project.sharist.data.local.RideRequestDao
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class RideRequestRepository(
    private val rideRequestDao: RideRequestDao? = null
) {

    private val rideRequestsTable = supabase.postgrest["ride_requests"]

    suspend fun insert(request: RideRequestEntity): GenericResult<RideRequestInsertResult> {
        val result = safeSupabaseCall {
            rideRequestsTable.insert(request.copy(pendingSync = false))
            rideRequestDao?.insert(request.copy(pendingSync = false))
            RideRequestInsertResult.Synced
        }

        if (result is GenericResult.Error && result.error == AppError.Network) {
            rideRequestDao?.insert(request.copy(pendingSync = true))
            return GenericResult.Success(RideRequestInsertResult.PendingSync)
        }

        return result
    }

    suspend fun syncPendingRequest(request: RideRequestEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            rideRequestsTable.insert(request.copy(pendingSync = false))
            rideRequestDao?.markSynced(request.id)
        }
    }

    suspend fun getPendingFutureRequestsByPassenger(passengerId: String, after: String): List<RideRequestEntity> {
        return rideRequestDao?.getPendingFutureRequestsByPassenger(passengerId, after).orEmpty()
    }

    suspend fun getPendingRequest(requestId: String): RideRequestEntity? {
        return rideRequestDao?.getPendingById(requestId)
    }

    suspend fun savePendingRequest(request: RideRequestEntity) {
        rideRequestDao?.insert(request.copy(pendingSync = true))
    }

    suspend fun deletePendingRequest(requestId: String) {
        rideRequestDao?.deleteById(requestId)
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
        val cachedRequests = getCachedFutureRequestsByPassenger(passengerId, after, from, to)

        if (cachedRequests.hasFullPage(from, to)) {
            return GenericResult.Success(cachedRequests)
        }

        val result = safeSupabaseCall {
            rideRequestsTable.select {
                filter {
                    eq("passenger_id", passengerId)
                    gte("departure_time", after)
                }
                order("departure_time", Order.ASCENDING)
                range(from, to)
            }.decodeList<RideRequestEntity>()
                .also { requests -> cacheRequests(requests) }
        }

        if (result is GenericResult.Error && result.error == AppError.Network && cachedRequests.isNotEmpty()) {
            return GenericResult.Success(cachedRequests)
        }

        return result
    }

    private suspend fun getCachedFutureRequestsByPassenger(
        passengerId: String,
        after: String,
        from: Long,
        to: Long
    ): List<RideRequestEntity> {
        return rideRequestDao?.getCachedFutureRequestsByPassenger(
            passengerId = passengerId,
            after = after,
            limit = (to - from + 1).toInt(),
            offset = from.toInt()
        ).orEmpty()
    }

    suspend fun delete(requestId: String): GenericResult<Unit> {
        return safeSupabaseCall {
            rideRequestsTable.delete {
                filter {
                    eq("id", requestId)
                }
            }
            rideRequestDao?.deleteById(requestId)
        }
    }

    private suspend fun cacheRequests(requests: List<RideRequestEntity>) {
        if (requests.isEmpty()) return
        rideRequestDao?.insertAll(requests.map { it.copy(pendingSync = false) })
    }

    private fun List<RideRequestEntity>.hasFullPage(from: Long, to: Long): Boolean {
        return size == (to - from + 1).toInt()
    }
}

enum class RideRequestInsertResult {
    Synced,
    PendingSync
}

private const val DEFAULT_PAGE_SIZE = 10L
