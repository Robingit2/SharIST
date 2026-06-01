package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.model.review.UserCommentInput
import com.project.sharist.data.repository.UserCommentRepository

class GiveOrUpdateCommentUseCase (
    private val userCommentRepository: UserCommentRepository
) {
    suspend operator fun invoke(data: UserCommentInput) : GenericResult<Unit> {
        val existingComment = when (val result = userCommentRepository.getCommentByUsers(data.raterId, data.targetId)) {
            is GenericResult.Success -> result.data
            is GenericResult.Error -> return result
        }

        return userCommentRepository.upsert(
            UserComment(
                id = existingComment?.id ?: java.util.UUID.randomUUID().toString(),
                raterUserId = data.raterId,
                targetUserId = data.targetId,
                comment = data.comment,
                createdAt = existingComment?.createdAt
            )
        )
    }
}
