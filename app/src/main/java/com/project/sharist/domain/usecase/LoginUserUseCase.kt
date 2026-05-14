package com.project.sharist.domain.usecase

import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.model.User

class LoginUserUseCase(private val repository: UserRepository) {

    suspend operator fun invoke(email: String): User? {
        return repository.loginUser(email)
    }
}