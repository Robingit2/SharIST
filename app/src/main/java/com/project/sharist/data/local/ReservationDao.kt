package com.project.sharist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.sharist.data.model.ride.ReservationEntity

@Dao
interface ReservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: ReservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Query("""
        SELECT * FROM reservations
        WHERE passengerId = :passengerId
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
    """)
    suspend fun getFreshByPassenger(passengerId: String, minFetchedAtMillis: Long): List<ReservationEntity>

    @Query("""
        SELECT * FROM reservations
        WHERE rideOfferId IN (:offerIds)
            AND cacheFetchedAtMillis >= :minFetchedAtMillis
    """)
    suspend fun getFreshByOffers(offerIds: List<String>, minFetchedAtMillis: Long): List<ReservationEntity>

    @Query("UPDATE reservations SET cacheLastAccessedAtMillis = :accessedAtMillis WHERE passengerId = :passengerId")
    suspend fun updateLastAccessedByPassenger(passengerId: String, accessedAtMillis: Long)

    @Query("UPDATE reservations SET cacheLastAccessedAtMillis = :accessedAtMillis WHERE rideOfferId IN (:offerIds)")
    suspend fun updateLastAccessedByOffers(offerIds: List<String>, accessedAtMillis: Long)

    @Query("DELETE FROM reservations WHERE passengerId = :passengerId")
    suspend fun deleteByPassenger(passengerId: String)

    @Query("DELETE FROM reservations WHERE rideOfferId IN (:offerIds)")
    suspend fun deleteByOffers(offerIds: List<String>)

    @Query("""
        DELETE FROM reservations
        WHERE (rideOfferId || ':' || passengerId) NOT IN (
            SELECT rideOfferId || ':' || passengerId FROM reservations
            ORDER BY cacheLastAccessedAtMillis DESC
            LIMIT :limit
        )
    """)
    suspend fun trimToLimit(limit: Int)
}
