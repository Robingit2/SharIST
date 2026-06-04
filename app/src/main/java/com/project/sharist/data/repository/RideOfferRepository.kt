package com.project.sharist.data.repository

import com.project.sharist.data.mapper.toEntity
import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlin.math.cos
import kotlin.math.max

class RideOfferRepository {

    private val rideOffersTable = supabase.postgrest["ride_offers"]

    suspend fun insert(offer: RideOfferEntity) {
        rideOffersTable.insert(offer)
    }

    suspend fun getFutureOffers(after: String): List<RideOfferEntity> {
        return rideOffersTable.select {
            filter {
                gte("departure_time", after)
            }
        }.decodeList()
    }

    suspend fun getFutureOffersByIds(offerIds: List<String>, after: String): List<RideOfferEntity> {
        val ids = offerIds.distinct()
        if (ids.isEmpty()) return emptyList()

        return rideOffersTable.select {
            filter {
                isIn("id", ids)
                gte("departure_time", after)
            }
            order("departure_time", Order.ASCENDING)
        }.decodeList()
    }

    suspend fun getPastOffersByIds(offerIds: List<String>, before: String): List<RideOfferEntity> {
        return getPastOffersByIds(offerIds, before, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getPastOffersByIds(offerIds: List<String>, before: String, from: Long, to: Long): List<RideOfferEntity> {
        val ids = offerIds.distinct()
        if (ids.isEmpty()) return emptyList()

        return rideOffersTable.select {
            filter {
                isIn("id", ids)
                lt("departure_time", before)
            }
            order("departure_time", Order.DESCENDING)
            range(from, to)
        }.decodeList()
    }

    suspend fun getOffersByDriver(driverId: String): List<RideOfferEntity> {
        return rideOffersTable.select {
            filter {
                eq("driver_id", driverId)
            }
        }.decodeList()
    }

    suspend fun getFutureOffersByDriver(driverId: String, after: String): List<RideOfferEntity> {
        return getFutureOffersByDriver(driverId, after, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getFutureOffersByDriver(driverId: String, after: String, from: Long, to: Long): List<RideOfferEntity> {
        return rideOffersTable.select {
            filter {
                eq("driver_id", driverId)
                gte("departure_time", after)
            }
            order("departure_time", Order.ASCENDING)
            range(from, to)
        }.decodeList()
    }

    suspend fun getPastOffersByDriver(driverId: String, before: String): List<RideOfferEntity> {
        return getPastOffersByDriver(driverId, before, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getPastOffersByDriver(driverId: String, before: String, from: Long, to: Long): List<RideOfferEntity> {
        return rideOffersTable.select {
            filter {
                eq("driver_id", driverId)
                lt("departure_time", before)
            }
            order("departure_time", Order.DESCENDING)
            range(from, to)
        }.decodeList()
    }

    suspend fun getOffers(filter: RideRequest): List<RideOfferEntity> {
        return getOffers(filter, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getOffers(filter: RideRequest, from: Long, to: Long): List<RideOfferEntity> {
        val request = filter.toEntity()
        val departureBounds = coordinateBounds(request.departureLat, request.departureRadiusMeters)
        val arrivalBounds = coordinateBounds(request.arrivalLat, request.arrivalRadiusMeters)
        val departureStart = (filter.desiredDepartureTimeMillis - filter.departureToleranceMinutes.minutesToMillis())
            .toTimestampz()
        val departureEnd = (filter.desiredDepartureTimeMillis + filter.departureToleranceMinutes.minutesToMillis())
            .toTimestampz()

        return rideOffersTable
            .select {
                filter {
                    neq("driver_id", request.passengerId)
                    eq("recurrence_type", request.recurringType)
                    gte("departure_time", System.currentTimeMillis().toTimestampz())
                    gte("departure_time", departureStart)
                    lte("departure_time", departureEnd)
                    gte("departure_latitude", request.departureLat - departureBounds.latitudeDelta)
                    lte("departure_latitude", request.departureLat + departureBounds.latitudeDelta)
                    gte("departure_longitude", request.departureLng - departureBounds.longitudeDelta)
                    lte("departure_longitude", request.departureLng + departureBounds.longitudeDelta)
                    gte("arrival_latitude", request.arrivalLat - arrivalBounds.latitudeDelta)
                    lte("arrival_latitude", request.arrivalLat + arrivalBounds.latitudeDelta)
                    gte("arrival_longitude", request.arrivalLng - arrivalBounds.longitudeDelta)
                    lte("arrival_longitude", request.arrivalLng + arrivalBounds.longitudeDelta)
                }
                order("departure_time", Order.ASCENDING)
                range(from, to)
            }
            .decodeList()
    }

    suspend fun delete(offerId: String) {
        rideOffersTable.delete {
            filter {
                eq("id", offerId)
            }
        }
    }
}

private data class CoordinateBounds(
    val latitudeDelta: Double,
    val longitudeDelta: Double
)

private fun coordinateBounds(latitude: Double, radiusMeters: Int): CoordinateBounds {
    val latitudeDelta = radiusMeters / METERS_PER_LATITUDE_DEGREE
    val longitudeMetersPerDegree = max(
        MIN_LONGITUDE_METERS_PER_DEGREE,
        METERS_PER_LATITUDE_DEGREE * cos(Math.toRadians(latitude))
    )

    return CoordinateBounds(
        latitudeDelta = latitudeDelta,
        longitudeDelta = radiusMeters / longitudeMetersPerDegree
    )
}

private fun Int.minutesToMillis(): Long = this * 60_000L

private fun Long.toTimestampz(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(java.util.Date(this))
        .replace("+0000", "+00:00")
}

private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private const val MIN_LONGITUDE_METERS_PER_DEGREE = 1.0
private const val DEFAULT_FILTERED_LIMIT = 10L
