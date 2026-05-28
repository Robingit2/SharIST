package com.project.sharist.data.repository

import com.project.sharist.data.model.ride.RideOfferEntity
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class RideOfferRepository {

    private val rideOffersTable = supabase.postgrest["ride_offers"]

    suspend fun insert(offer: RideOfferEntity) {
        rideOffersTable.insert(offer)
    }

    suspend fun getOffers(): List<RideOfferEntity> {
        return rideOffersTable.select().decodeList()
    }

    suspend fun delete(offerId: String) {
        rideOffersTable.delete {
            filter {
                eq("id", offerId)
            }
        }
    }
}
