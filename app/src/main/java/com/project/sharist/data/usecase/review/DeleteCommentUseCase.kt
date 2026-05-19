package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.repository.UserCommentRepository

class DeleteCommentUseCase(
    private val userCommentRepository: UserCommentRepository
) {
    suspend operator fun invoke(raterId: String, targetId: String) : GenericResult<Unit> {
        return userCommentRepository.delete(raterId, targetId)
    }
}
