package com.project.sharist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.sharist.data.model.ride.RideOfferEntity

@Dao
interface RideOfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: RideOfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<RideOfferEntity>)

    @Query("""
        SELECT * FROM ride_offers
        WHERE id IN (:offerIds)
            AND departureTime >= :after
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
        ORDER BY departureTime ASC
    """)
    suspend fun getFreshFutureOffersByIds(
        offerIds: List<String>,
        after: String,
        minFetchedAtMillis: Long
    ): List<RideOfferEntity>

    @Query("""
        SELECT * FROM ride_offers
        WHERE id IN (:offerIds)
            AND departureTime < :before
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
        ORDER BY departureTime DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFreshPastOffersByIds(
        offerIds: List<String>,
        before: String,
        minFetchedAtMillis: Long,
        limit: Int,
        offset: Int
    ): List<RideOfferEntity>

    @Query("""
        SELECT * FROM ride_offers
        WHERE driverId = :driverId
            AND departureTime >= :after
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
        ORDER BY departureTime ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFreshFutureOffersByDriver(
        driverId: String,
        after: String,
        minFetchedAtMillis: Long,
        limit: Int,
        offset: Int
    ): List<RideOfferEntity>

    @Query("""
        SELECT * FROM ride_offers
        WHERE driverId = :driverId
            AND departureTime >= :after
            AND pendingSync = 0
        ORDER BY departureTime ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getCachedFutureOffersByDriver(
        driverId: String,
        after: String,
        limit: Int,
        offset: Int
    ): List<RideOfferEntity>

    @Query("""
        SELECT * FROM ride_offers
        WHERE pendingSync = 1
            AND driverId = :driverId
            AND departureTime >= :after
        ORDER BY departureTime ASC
    """)
    suspend fun getPendingFutureOffersByDriver(driverId: String, after: String): List<RideOfferEntity>

    @Query("SELECT * FROM ride_offers WHERE id = :offerId AND pendingSync = 1 LIMIT 1")
    suspend fun getPendingById(offerId: String): RideOfferEntity?

    @Query("UPDATE ride_offers SET pendingSync = 0, cacheFetchedAtMillis = :syncedAtMillis, cacheLastAccessedAtMillis = :syncedAtMillis WHERE id = :offerId")
    suspend fun markSynced(offerId: String, syncedAtMillis: Long)

    @Query("""
        SELECT * FROM ride_offers
        WHERE driverId = :driverId
            AND departureTime < :before
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
        ORDER BY departureTime DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFreshPastOffersByDriver(
        driverId: String,
        before: String,
        minFetchedAtMillis: Long,
        limit: Int,
        offset: Int
    ): List<RideOfferEntity>

    @Query("""
        SELECT * FROM ride_offers
        WHERE driverId != :excludedDriverId
            AND recurringType = :recurringType
            AND pendingSync = 0
            AND departureTime >= :now
            AND departureTime >= :departureStart
            AND departureTime <= :departureEnd
            AND departureLat >= :departureMinLat
            AND departureLat <= :departureMaxLat
            AND departureLng >= :departureMinLng
            AND departureLng <= :departureMaxLng
            AND arrivalLat >= :arrivalMinLat
            AND arrivalLat <= :arrivalMaxLat
            AND arrivalLng >= :arrivalMinLng
            AND arrivalLng <= :arrivalMaxLng
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
        ORDER BY departureTime ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFreshOffersMatchingFilter(
        excludedDriverId: String,
        recurringType: String,
        now: String,
        departureStart: String,
        departureEnd: String,
        departureMinLat: Double,
        departureMaxLat: Double,
        departureMinLng: Double,
        departureMaxLng: Double,
        arrivalMinLat: Double,
        arrivalMaxLat: Double,
        arrivalMinLng: Double,
        arrivalMaxLng: Double,
        minFetchedAtMillis: Long,
        limit: Int,
        offset: Int
    ): List<RideOfferEntity>

    @Query("UPDATE ride_offers SET cacheLastAccessedAtMillis = :accessedAtMillis WHERE id IN (:offerIds)")
    suspend fun updateLastAccessed(offerIds: List<String>, accessedAtMillis: Long)

    @Query("DELETE FROM ride_offers WHERE id = :offerId")
    suspend fun deleteById(offerId: String)

    @Query("DELETE FROM ride_offers")
    suspend fun clearOffers()

    @Query("""
        DELETE FROM ride_offers
        WHERE id NOT IN (
            SELECT id FROM ride_offers
            ORDER BY cacheLastAccessedAtMillis DESC
            LIMIT :limit
        )
    """)
    suspend fun trimToLimit(limit: Int)
}
