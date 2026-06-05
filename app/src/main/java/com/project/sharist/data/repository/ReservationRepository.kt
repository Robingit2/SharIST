package com.project.sharist.data.repository

import com.project.sharist.data.local.ReservationDao
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.ReservationEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class ReservationRepository(
    private val reservationDao: ReservationDao? = null
) {

    private val reservationsTable = supabase.postgrest["reservations"]

    suspend fun insert(reservation: ReservationEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            reservationsTable.insert(reservation)
            cacheReservations(listOf(reservation))
        }
    }

    suspend fun getReservationsByPassenger(passengerId: String): GenericResult<List<ReservationEntity>> {
        val result = safeSupabaseCall {
            reservationsTable.select {
                filter {
                    eq("passenger_id", passengerId)
                }
            }.decodeList<ReservationEntity>()
                .also { reservations -> cachePassengerReservations(passengerId, reservations) }
        }

        if (result is GenericResult.Error && result.error == AppError.Network) {
            val cachedReservations = reservationDao
                ?.getFreshByPassenger(passengerId, minFreshFetchedAtMillis())
                .orEmpty()
            if (cachedReservations.isNotEmpty()) {
                reservationDao?.updateLastAccessedByPassenger(passengerId, System.currentTimeMillis())
                return GenericResult.Success(cachedReservations)
            }
        }

        return result
    }

    suspend fun getReservationsByOffers(offerIds: List<String>): GenericResult<List<ReservationEntity>> {
        if (offerIds.isEmpty()) return GenericResult.Success(emptyList())

        val ids = offerIds.distinct()
        val result = safeSupabaseCall {
            reservationsTable.select {
                filter {
                    isIn("ride_offer_id", ids)
                }
            }.decodeList<ReservationEntity>()
                .also { reservations -> cacheOfferReservations(ids, reservations) }
        }

        if (result is GenericResult.Error && result.error == AppError.Network) {
            val cachedReservations = reservationDao
                ?.getFreshByOffers(ids, minFreshFetchedAtMillis())
                .orEmpty()
            if (cachedReservations.isNotEmpty()) {
                reservationDao?.updateLastAccessedByOffers(ids, System.currentTimeMillis())
                return GenericResult.Success(cachedReservations)
            }
        }

        return result
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

    private suspend fun cachePassengerReservations(
        passengerId: String,
        reservations: List<ReservationEntity>
    ) {
        reservationDao?.deleteByPassenger(passengerId)
        cacheReservations(reservations)
    }

    private suspend fun cacheOfferReservations(
        offerIds: List<String>,
        reservations: List<ReservationEntity>
    ) {
        reservationDao?.deleteByOffers(offerIds)
        cacheReservations(reservations)
    }

    private suspend fun cacheReservations(reservations: List<ReservationEntity>) {
        if (reservations.isEmpty()) return

        val now = System.currentTimeMillis()
        reservationDao?.insertAll(
            reservations.map { reservation ->
                reservation.copy(
                    cacheLastAccessedAtMillis = now,
                    cacheFetchedAtMillis = now
                )
            }
        )
        reservationDao?.trimToLimit(RESERVATION_CACHE_LIMIT)
    }

    private fun minFreshFetchedAtMillis(): Long {
        return System.currentTimeMillis() - RESERVATION_CACHE_TTL_MILLIS
    }
}

private const val RESERVATION_CACHE_LIMIT = 1_000
private const val RESERVATION_CACHE_TTL_MILLIS = 5 * 60 * 1000L
