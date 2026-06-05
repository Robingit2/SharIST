package com.project.sharist.data.usecase.user

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.error.AppError
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.SessionRepository
import com.project.sharist.data.repository.UserRepository
import kotlinx.coroutines.flow.first

class SetActiveRoleUseCase (
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(userId: String, role: RoleType): GenericResult<Unit> {
        val cachedRoles = sessionRepository.getUserRoles(userId).first()
        val roles = cachedRoles.ifEmpty {
            when (val result = userRepository.getUserRoles(userId)) {
                is GenericResult.Success -> {
                    sessionRepository.setUserRoles(userId, result.data)
                    result.data
                }

                is GenericResult.Error -> return result
            }
        }

        if (role !in roles) {
            return GenericResult.Error(AppError.Unauthorized)
        }

        sessionRepository.setActiveRole(userId, role)
        return GenericResult.Success(Unit)
    }
}
