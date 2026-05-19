package com.project.sharist.data.repository

import com.project.sharist.data.local.RideOfferDao
import com.project.sharist.data.model.ride.RideOfferEntity

class RideOfferRepository(
    private val dao: RideOfferDao
) {

    suspend fun insert(offer: RideOfferEntity) =
        dao.insert(offer)

    fun getOffers() = dao.getAll()
}