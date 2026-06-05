package com.project.sharist.data.repository

import com.project.sharist.data.local.RideOfferDao
import com.project.sharist.data.mapper.toEntity
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.domain.model.RideRequest
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlin.math.cos
import kotlin.math.max

class RideOfferRepository(
    private val rideOfferDao: RideOfferDao? = null
) {

    private val rideOffersTable = supabase.postgrest["ride_offers"]

    suspend fun insert(offer: RideOfferEntity): GenericResult<RideOfferInsertResult> {
        val result = safeSupabaseCall {
            rideOffersTable.insert(offer.copy(pendingSync = false))
            cacheOffers(listOf(offer.copy(pendingSync = false)))
            RideOfferInsertResult.Synced
        }

        if (result is GenericResult.Error && result.error == AppError.Network) {
            savePendingOffer(offer)
            return GenericResult.Success(RideOfferInsertResult.PendingSync)
        }

        return result
    }

    suspend fun syncPendingOffer(offer: RideOfferEntity): GenericResult<Unit> {
        return safeSupabaseCall {
            rideOffersTable.insert(offer.copy(pendingSync = false))
            rideOfferDao?.markSynced(offer.id, System.currentTimeMillis())
        }
    }

    suspend fun getPendingFutureOffersByDriver(driverId: String, after: String): List<RideOfferEntity> {
        return rideOfferDao?.getPendingFutureOffersByDriver(driverId, after).orEmpty()
    }

    suspend fun getPendingOffer(offerId: String): RideOfferEntity? {
        return rideOfferDao?.getPendingById(offerId)
    }

    suspend fun savePendingOffer(offer: RideOfferEntity) {
        rideOfferDao?.insert(
            offer.copy(
                pendingSync = true,
                cacheFetchedAtMillis = 0L,
                cacheLastAccessedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePendingOffer(offerId: String) {
        rideOfferDao?.deleteById(offerId)
    }

    suspend fun getFutureOffers(after: String): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            rideOffersTable.select {
                filter {
                    gte("departure_time", after)
                }
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getFutureOffersByIds(offerIds: List<String>, after: String): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val ids = offerIds.distinct()
            if (ids.isEmpty()) return@safeSupabaseCall emptyList()

            val cachedOffers = rideOfferDao?.getFreshFutureOffersByIds(
                offerIds = ids,
                after = after,
                minFetchedAtMillis = minFreshFetchedAtMillis()
            )
            if (cachedOffers != null && cachedOffers.size == ids.size) {
                markAccessed(cachedOffers)
                return@safeSupabaseCall cachedOffers
            }

            rideOffersTable.select {
                filter {
                    isIn("id", ids)
                    gte("departure_time", after)
                }
                order("departure_time", Order.ASCENDING)
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getPastOffersByIds(offerIds: List<String>, before: String): GenericResult<List<RideOfferEntity>> {
        return getPastOffersByIds(offerIds, before, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getPastOffersByIds(offerIds: List<String>, before: String, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val ids = offerIds.distinct()
            if (ids.isEmpty()) return@safeSupabaseCall emptyList()
            val page = pageBounds(from, to)

            val cachedOffers = rideOfferDao?.getFreshPastOffersByIds(
                offerIds = ids,
                before = before,
                minFetchedAtMillis = minFreshFetchedAtMillis(),
                limit = page.limit,
                offset = page.offset
            )
            if (cachedOffers != null && cachedOffers.hasFullPage(page)) {
                markAccessed(cachedOffers)
                return@safeSupabaseCall cachedOffers
            }

            rideOffersTable.select {
                filter {
                    isIn("id", ids)
                    lt("departure_time", before)
                }
                order("departure_time", Order.DESCENDING)
                range(from, to)
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getOffersByDriver(driverId: String): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            rideOffersTable.select {
                filter {
                    eq("driver_id", driverId)
                }
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getFutureOffersByDriver(driverId: String, after: String): GenericResult<List<RideOfferEntity>> {
        return getFutureOffersByDriver(driverId, after, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getFutureOffersByDriver(driverId: String, after: String, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val page = pageBounds(from, to)
            val cachedOffers = rideOfferDao?.getFreshFutureOffersByDriver(
                driverId = driverId,
                after = after,
                minFetchedAtMillis = minFreshFetchedAtMillis(),
                limit = page.limit,
                offset = page.offset
            )
            if (cachedOffers != null && cachedOffers.hasFullPage(page)) {
                markAccessed(cachedOffers)
                return@safeSupabaseCall cachedOffers
            }

            rideOffersTable.select {
                filter {
                    eq("driver_id", driverId)
                    gte("departure_time", after)
                }
                order("departure_time", Order.ASCENDING)
                range(from, to)
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getPastOffersByDriver(driverId: String, before: String): GenericResult<List<RideOfferEntity>> {
        return getPastOffersByDriver(driverId, before, from = 0, to = DEFAULT_FILTERED_LIMIT - 1)
    }

    suspend fun getPastOffersByDriver(driverId: String, before: String, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val page = pageBounds(from, to)
            val cachedOffers = rideOfferDao?.getFreshPastOffersByDriver(
                driverId = driverId,
                before = before,
                minFetchedAtMillis = minFreshFetchedAtMillis(),
                limit = page.limit,
                offset = page.offset
            )
            if (cachedOffers != null && cachedOffers.hasFullPage(page)) {
                markAccessed(cachedOffers)
                return@safeSupabaseCall cachedOffers
            }

            rideOffersTable.select {
                filter {
                    eq("driver_id", driverId)
                    lt("departure_time", before)
                }
                order("departure_time", Order.DESCENDING)
                range(from, to)
            }.decodeList<RideOfferEntity>()
                .also { offers -> cacheOffers(offers) }
        }
    }

    suspend fun getFilteredOffers(filter: RideRequest, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val query = filter.toOfferFilterQuery()
            val page = pageBounds(from, to)

            val cachedOffers = getCachedFilteredOffers(query, page)
            if (cachedOffers.hasFullPage(page)) {
                markAccessed(cachedOffers)
                return@safeSupabaseCall cachedOffers
            }

            fetchFilteredOffers(query, from, to)
        }
    }

    suspend fun getCachedFilteredOffers(filter: RideRequest, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            val page = pageBounds(from, to)
            val cachedOffers = getCachedFilteredOffers(filter.toOfferFilterQuery(), page)
            markAccessed(cachedOffers)
            cachedOffers
        }
    }

    suspend fun refreshFilteredOffers(filter: RideRequest, from: Long, to: Long): GenericResult<List<RideOfferEntity>> {
        return safeSupabaseCall {
            fetchFilteredOffers(filter.toOfferFilterQuery(), from, to)
        }
    }

    private suspend fun getCachedFilteredOffers(
        query: OfferFilterQuery,
        page: PageBounds
    ): List<RideOfferEntity> {
        return rideOfferDao?.getFreshOffersMatchingFilter(
            excludedDriverId = query.excludedDriverId,
            recurringType = query.recurringType,
            now = query.now,
            departureStart = query.departureStart,
            departureEnd = query.departureEnd,
            departureMinLat = query.departureMinLat,
            departureMaxLat = query.departureMaxLat,
            departureMinLng = query.departureMinLng,
            departureMaxLng = query.departureMaxLng,
            arrivalMinLat = query.arrivalMinLat,
            arrivalMaxLat = query.arrivalMaxLat,
            arrivalMinLng = query.arrivalMinLng,
            arrivalMaxLng = query.arrivalMaxLng,
            minFetchedAtMillis = minFreshFetchedAtMillis(),
            limit = page.limit,
            offset = page.offset
        ).orEmpty()
    }

    private suspend fun fetchFilteredOffers(query: OfferFilterQuery, from: Long, to: Long): List<RideOfferEntity> {
        return rideOffersTable
            .select {
                filter {
                    neq("driver_id", query.excludedDriverId)
                    eq("recurrence_type", query.recurringType)
                    gte("departure_time", query.now)
                    gte("departure_time", query.departureStart)
                    lte("departure_time", query.departureEnd)
                    gte("departure_latitude", query.departureMinLat)
                    lte("departure_latitude", query.departureMaxLat)
                    gte("departure_longitude", query.departureMinLng)
                    lte("departure_longitude", query.departureMaxLng)
                    gte("arrival_latitude", query.arrivalMinLat)
                    lte("arrival_latitude", query.arrivalMaxLat)
                    gte("arrival_longitude", query.arrivalMinLng)
                    lte("arrival_longitude", query.arrivalMaxLng)
                }
                order("departure_time", Order.ASCENDING)
                range(from, to)
            }
            .decodeList<RideOfferEntity>()
            .also { offers -> cacheOffers(offers) }
    }

    private fun RideRequest.toOfferFilterQuery(): OfferFilterQuery {
        val request = toEntity()
        val departureBounds = coordinateBounds(request.departureLat, request.departureRadiusMeters)
        val arrivalBounds = coordinateBounds(request.arrivalLat, request.arrivalRadiusMeters)
        val departureStart = (desiredDepartureTimeMillis - departureToleranceMinutes.minutesToMillis())
            .toTimestampz()
        val departureEnd = (desiredDepartureTimeMillis + departureToleranceMinutes.minutesToMillis())
            .toTimestampz()
        return OfferFilterQuery(
            excludedDriverId = request.passengerId,
            recurringType = request.recurringType,
            now = System.currentTimeMillis().toTimestampz(),
            departureStart = departureStart,
            departureEnd = departureEnd,
            departureMinLat = request.departureLat - departureBounds.latitudeDelta,
            departureMaxLat = request.departureLat + departureBounds.latitudeDelta,
            departureMinLng = request.departureLng - departureBounds.longitudeDelta,
            departureMaxLng = request.departureLng + departureBounds.longitudeDelta,
            arrivalMinLat = request.arrivalLat - arrivalBounds.latitudeDelta,
            arrivalMaxLat = request.arrivalLat + arrivalBounds.latitudeDelta,
            arrivalMinLng = request.arrivalLng - arrivalBounds.longitudeDelta,
            arrivalMaxLng = request.arrivalLng + arrivalBounds.longitudeDelta
        )
    }

    suspend fun delete(offerId: String): GenericResult<Unit> {
        return safeSupabaseCall {
            rideOffersTable.delete {
                filter {
                    eq("id", offerId)
                }
            }
            rideOfferDao?.deleteById(offerId)
        }
    }

    suspend fun clearCachedOffers(): GenericResult<Unit> {
        return safeSupabaseCall {
            rideOfferDao?.clearOffers()
        }
    }

    private suspend fun cacheOffers(offers: List<RideOfferEntity>) {
        if (offers.isEmpty()) return
        val now = System.currentTimeMillis()
        rideOfferDao?.insertAll(
            offers.map { offer ->
                offer.copy(
                    cacheLastAccessedAtMillis = now,
                    cacheFetchedAtMillis = now,
                    pendingSync = false
                )
            }
        )
        rideOfferDao?.trimToLimit(RIDE_OFFER_CACHE_LIMIT)
    }

    private suspend fun markAccessed(offers: List<RideOfferEntity>) {
        if (offers.isEmpty()) return
        rideOfferDao?.updateLastAccessed(
            offerIds = offers.map { it.id },
            accessedAtMillis = System.currentTimeMillis()
        )
    }

    private fun minFreshFetchedAtMillis(): Long {
        return System.currentTimeMillis() - RIDE_OFFER_CACHE_TTL_MILLIS
    }
}

enum class RideOfferInsertResult {
    Synced,
    PendingSync
}

private data class CoordinateBounds(
    val latitudeDelta: Double,
    val longitudeDelta: Double
)

private data class OfferFilterQuery(
    val excludedDriverId: String,
    val recurringType: String,
    val now: String,
    val departureStart: String,
    val departureEnd: String,
    val departureMinLat: Double,
    val departureMaxLat: Double,
    val departureMinLng: Double,
    val departureMaxLng: Double,
    val arrivalMinLat: Double,
    val arrivalMaxLat: Double,
    val arrivalMinLng: Double,
    val arrivalMaxLng: Double
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

private data class PageBounds(
    val limit: Int,
    val offset: Int
)

private fun pageBounds(from: Long, to: Long): PageBounds {
    return PageBounds(
        limit = (to - from + 1).coerceAtLeast(0).toInt(),
        offset = from.coerceAtLeast(0).toInt()
    )
}

private fun List<RideOfferEntity>.hasFullPage(page: PageBounds): Boolean {
    return size >= page.limit
}

private fun Long.toTimestampz(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(java.util.Date(this))
        .replace("+0000", "+00:00")
}

private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private const val MIN_LONGITUDE_METERS_PER_DEGREE = 1.0
private const val DEFAULT_FILTERED_LIMIT = 10L
private const val RIDE_OFFER_CACHE_LIMIT = 200
private const val RIDE_OFFER_CACHE_TTL_MILLIS = 5 * 60 * 1000L
