package com.project.sharist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.sharist.data.model.user.User
import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.data.model.ride.RideRequestEntity
import com.project.sharist.data.model.ride.ReservationEntity
import androidx.room.TypeConverters
@Database(
    entities = [
        User::class,
        RideOfferEntity::class,
        RideRequestEntity::class,
        ReservationEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun rideOfferDao(): RideOfferDao
    abstract fun rideRequestDao(): RideRequestDao
    abstract fun reservationDao(): ReservationDao
}
