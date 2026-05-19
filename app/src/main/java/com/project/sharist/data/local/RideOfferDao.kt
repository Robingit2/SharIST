package com.project.sharist.data.local

import androidx.room.*
import com.project.sharist.data.model.ride.RideOfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideOfferDao {

    @Insert
    suspend fun insert(offer: RideOfferEntity)

    @Query("SELECT * FROM ride_offers")
    fun getAll(): Flow<List<RideOfferEntity>>
}