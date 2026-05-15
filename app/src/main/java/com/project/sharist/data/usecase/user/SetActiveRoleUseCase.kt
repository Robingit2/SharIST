package com.project.sharist.data.usecase.user

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.SessionRepository
import com.project.sharist.data.repository.UserRepository

class SetActiveRoleUseCase (
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(userId: String, role: RoleType) {
        val roles = userRepository.getUserRoles(userId)

        if (role !in roles) {
            throw IllegalArgumentException("User doesn't have this role")
        }

        sessionRepository.setActiveRole(role)
    }
}