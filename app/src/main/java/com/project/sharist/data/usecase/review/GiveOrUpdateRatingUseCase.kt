package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.data.model.review.UserRatingInput
import com.project.sharist.data.repository.UserRatingRepository

class GiveOrUpdateRatingUseCase (
    private val userRatingRepository: UserRatingRepository
) {
    suspend operator fun invoke(data: UserRatingInput) : GenericResult<Unit> {
        return userRatingRepository.upsert(
            UserRating(
                raterUserId = data.raterId,
                targetUserId = data.targetId,
                rating = data.rating
            )
        )

    }
}