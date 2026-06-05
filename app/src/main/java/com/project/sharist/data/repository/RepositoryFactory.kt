package com.project.sharist.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.project.sharist.data.local.DatabaseProvider

private val Context.sessionDataStore by preferencesDataStore(name = "session")

fun cachedUserRepository(context: Context): UserRepository {
    return UserRepository(
        userDao = DatabaseProvider.getDatabase(context).userDao()
    )
}

fun cachedRideOfferRepository(context: Context): RideOfferRepository {
    return RideOfferRepository(
        rideOfferDao = DatabaseProvider.getDatabase(context).rideOfferDao()
    )
}

fun cachedRideRequestRepository(context: Context): RideRequestRepository {
    return RideRequestRepository(
        rideRequestDao = DatabaseProvider.getDatabase(context).rideRequestDao()
    )
}

fun sessionRepository(context: Context): SessionRepository {
    return SessionRepository(context.applicationContext.sessionDataStore)
}
