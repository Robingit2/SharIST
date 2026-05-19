package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.review.UserCommentInput
import com.project.sharist.data.repository.UserCommentRepository

class GiveOrUpdateCommentUseCase (
    private val userCommentRepository: UserCommentRepository
) {
    suspend operator fun invoke(data: UserCommentInput) : GenericResult<Unit> {
        return userCommentRepository.upsert(
            UserComment(
                raterUserId = data.raterId,
                targetUserId = data.targetId,
                comment = data.comment
            )
        )
    }
}