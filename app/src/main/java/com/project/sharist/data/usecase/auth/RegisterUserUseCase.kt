package com.project.sharist.data.usecase.auth

import com.project.sharist.data.model.auth.RegisterResult
import com.project.sharist.data.model.auth.RegisterUserInput
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.model.user.User
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class RegisterUserUseCase(
    private val repository: UserRepository
) {

    suspend operator fun invoke(data: RegisterUserInput) : RegisterResult {

        val authUser = supabase.auth.signUpWith(Email) {
            email = data.email
            password = data.password
        }

        val userId = authUser?.id
            ?: return RegisterResult.Failure("User creation failed or is pending confirmation")

        val newUser = User(
            id = userId,
            name = data.name,
            photoPath = data.photoPath
        )

        repository.insert(newUser)
        return RegisterResult.Success
    }
}