package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.helpers.isUniqueViolation
import com.project.sharist.data.model.review.GiveRatingResult
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.data.model.review.UserRatingInput
import com.project.sharist.data.repository.UserRatingRepository

class GiveRatingUseCase (
    private val userRatingRepository: UserRatingRepository
) {
    suspend operator fun invoke(data: UserRatingInput) : GiveRatingResult {
        try {
            userRatingRepository.insert(
                UserRating(
                    raterUserId = data.raterId,
                    targetUserId = data.targetId,
                    rating = data.rating
                )
            )
            return GiveRatingResult.Success
        } catch (e: Exception) {
            if (isUniqueViolation(e)) {
                return GiveRatingResult.AlreadyExists
            }
            return GiveRatingResult.Failure(e.message)
        }
    }
}