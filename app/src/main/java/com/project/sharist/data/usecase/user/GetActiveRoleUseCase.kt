package com.project.sharist.data.usecase.user

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class GetActiveRoleUseCase (
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(userId: String) : Flow<RoleType?> {
        return sessionRepository.getActiveRole()
    }
}