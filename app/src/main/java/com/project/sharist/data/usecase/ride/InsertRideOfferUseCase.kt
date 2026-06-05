package com.project.sharist.data.usecase.ride

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.mapper.toEntity
import com.project.sharist.domain.model.RideOffer

class InsertRideOfferUseCase(
    private val repo: RideOfferRepository
) {
    suspend operator fun invoke(offer: RideOffer): GenericResult<Unit> {
        return repo.insert(offer.toEntity())
    }
}
