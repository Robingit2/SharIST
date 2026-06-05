package com.project.sharist.data.usecase.user

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.SessionRepository
import com.project.sharist.data.repository.UserRepository

class SetActiveRoleUseCase (
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(userId: String, role: RoleType): GenericResult<Unit> {
        val roles = when (val result = userRepository.getUserRoles(userId)) {
            is GenericResult.Success -> result.data
            is GenericResult.Error -> return result
        }

        if (role !in roles) {
            return GenericResult.Error(com.project.sharist.data.model.error.AppError.Unauthorized)
        }

        sessionRepository.setActiveRole(userId, role)
        return GenericResult.Success(Unit)
    }
}
