package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.repository.UserRatingRepository

class DeleteRatingUseCase(
    private val userRatingRepository: UserRatingRepository
) {
    suspend operator fun invoke(raterId: String, targetId: String) : GenericResult<Unit> {
        return userRatingRepository.delete(raterId, targetId)
    }
}