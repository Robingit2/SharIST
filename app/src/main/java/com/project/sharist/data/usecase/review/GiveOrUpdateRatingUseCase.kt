package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.data.model.review.UserRatingInput
import com.project.sharist.data.repository.UserRatingRepository

class GiveOrUpdateRatingUseCase (
    private val userRatingRepository: UserRatingRepository
) {
    suspend operator fun invoke(data: UserRatingInput) : GenericResult<Unit> {
        val existingRating = when (val result = userRatingRepository.getRatingByUsers(data.raterId, data.targetId)) {
            is GenericResult.Success -> result.data
            is GenericResult.Error -> return result
        }

        return userRatingRepository.upsert(
            UserRating(
                id = existingRating?.id ?: java.util.UUID.randomUUID().toString(),
                raterUserId = data.raterId,
                targetUserId = data.targetId,
                rating = data.rating,
                createdAt = existingRating?.createdAt
            )
        )

    }
}
