package com.project.sharist.data.local

import androidx.room.*
import com.project.sharist.data.model.ride.RideRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideRequestDao {

    @Insert
    suspend fun insert(request: RideRequestEntity)

    @Query("SELECT * FROM ride_requests")
    fun getAll(): Flow<List<RideRequestEntity>>
}