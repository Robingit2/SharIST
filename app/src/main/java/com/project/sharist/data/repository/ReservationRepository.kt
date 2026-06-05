package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.ReservationEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class ReservationRepository {

    private val reservationsTable = supabase.postgrest["reservations"]

    suspend fun insert(reservation: ReservationEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            reservationsTable.insert(reservation)
        }
    }

    suspend fun getReservationsByPassenger(passengerId: String): GenericResult<List<ReservationEntity>> {
        return safeSupabaseCall {
            reservationsTable.select {
                filter {
                    eq("passenger_id", passengerId)
                }
            }.decodeList()
        }
    }

    suspend fun getReservationsByOffers(offerIds: List<String>): GenericResult<List<ReservationEntity>> {
        if (offerIds.isEmpty()) return GenericResult.Success(emptyList())

        return safeSupabaseCall {
            reservationsTable.select {
                filter {
                    isIn("ride_offer_id", offerIds)
                }
            }.decodeList()
        }
    }

    suspend fun getReservationCountByOffer(offerId: String): GenericResult<Int> {
        return when (val result = getReservationsByOffers(listOf(offerId))) {
            is GenericResult.Success -> GenericResult.Success(result.data.size)
            is GenericResult.Error -> result
        }
    }

    suspend fun getReservationCountsByOffers(offerIds: List<String>): GenericResult<Map<String, Int>> {
        return when (val result = getReservationsByOffers(offerIds)) {
            is GenericResult.Success -> GenericResult.Success(
                result.data
                    .groupingBy { it.rideOfferId }
                    .eachCount()
            )
            is GenericResult.Error -> result
        }
    }
}
