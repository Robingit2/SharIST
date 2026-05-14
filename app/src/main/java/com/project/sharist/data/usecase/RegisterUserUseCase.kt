package com.project.sharist.domain.usecase

import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.model.User
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth

class RegisterUserUseCase(
    private val repository: UserRepository
) {

    suspend operator fun invoke(user: User) {

        supabase.auth.signUpWith(Email) {
            email = "valid.email@supabase.io"
            password = "example-password"
        }

        repository.insert(user)
    }
}