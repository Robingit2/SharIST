package com.project.sharist.data.repository

import com.project.sharist.data.local.RideRequestDao
import com.project.sharist.data.model.ride.RideRequestEntity

class RideRequestRepository(
    private val dao: RideRequestDao
) {

    suspend fun insert(request: RideRequestEntity) =
        dao.insert(request)

    fun getRequests() = dao.getAll()
}