package com.project.sharist.data.local

import androidx.room.*
import com.project.sharist.data.model.ride.RideRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: RideRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<RideRequestEntity>)

    @Query("SELECT * FROM ride_requests")
    fun getAll(): Flow<List<RideRequestEntity>>

    @Query("""
        SELECT * FROM ride_requests
        WHERE pendingSync = 1
            AND passengerId = :passengerId
            AND desiredDepartureTime >= :after
        ORDER BY desiredDepartureTime ASC
    """)
    suspend fun getPendingFutureRequestsByPassenger(passengerId: String, after: String): List<RideRequestEntity>

    @Query("""
        SELECT * FROM ride_requests
        WHERE pendingSync = 0
            AND passengerId = :passengerId
            AND desiredDepartureTime >= :after
        ORDER BY desiredDepartureTime ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getCachedFutureRequestsByPassenger(
        passengerId: String,
        after: String,
        limit: Int,
        offset: Int
    ): List<RideRequestEntity>

    @Query("SELECT * FROM ride_requests WHERE id = :requestId AND pendingSync = 1 LIMIT 1")
    suspend fun getPendingById(requestId: String): RideRequestEntity?

    @Query("UPDATE ride_requests SET pendingSync = 0 WHERE id = :requestId")
    suspend fun markSynced(requestId: String)

    @Query("DELETE FROM ride_requests WHERE id = :requestId")
    suspend fun deleteById(requestId: String)
}
