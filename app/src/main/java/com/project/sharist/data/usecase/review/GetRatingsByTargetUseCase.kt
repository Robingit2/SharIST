package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserRating
import com.project.sharist.data.repository.UserRatingRepository

class GetRatingsByTargetUseCase (
    private val userRatingRepository: UserRatingRepository
) {
    suspend operator fun invoke(targetId: String) : GenericResult<List<UserRating>> {
        return userRatingRepository.getRatingsByTarget(targetId)
    }
}