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

    suspend fun getReservationsByPassenger(passengerId: String): List<ReservationEntity> {
        return reservationsTable.select {
            filter {
                eq("passenger_id", passengerId)
            }
        }.decodeList()
    }

    suspend fun getReservationsByOffers(offerIds: List<String>): List<ReservationEntity> {
        if (offerIds.isEmpty()) return emptyList()

        return reservationsTable.select {
            filter {
                isIn("ride_offer_id", offerIds)
            }
        }.decodeList()
    }

    suspend fun getReservationCountByOffer(offerId: String): Int {
        return getReservationsByOffers(listOf(offerId)).size
    }

    suspend fun getReservationCountsByOffers(offerIds: List<String>): Map<String, Int> {
        return getReservationsByOffers(offerIds)
            .groupingBy { it.rideOfferId }
            .eachCount()
    }
}
