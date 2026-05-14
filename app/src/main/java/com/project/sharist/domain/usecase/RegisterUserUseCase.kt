package com.project.sharist.domain.usecase

import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.model.User

class RegisterUserUseCase(
    private val repository: UserRepository
) {

    suspend operator fun invoke(user: User) {
        repository.registerUser(user)
    }
}