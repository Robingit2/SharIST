package com.project.sharist.data.usecase.review

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.review.UserComment
import com.project.sharist.data.repository.UserCommentRepository

class GetCommentsByTargetUseCase (
    private val userCommentRepository: UserCommentRepository
) {
    suspend operator fun invoke(targetId: String) : GenericResult<List<UserComment>> {
        return userCommentRepository.getCommentsByTarget(targetId)
    }
}
