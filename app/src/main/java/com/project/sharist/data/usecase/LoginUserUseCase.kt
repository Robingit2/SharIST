package com.project.sharist.data.usecase

import com.project.sharist.data.model.LoginResult
import com.project.sharist.data.model.LoginUserInput
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.model.User
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class LoginUserUseCase(private val repository: UserRepository) {

    suspend operator fun invoke(data: LoginUserInput): LoginResult {
        supabase.auth.signInWith(Email) {
            email = data.email
            password = data.password
        }

        val authUser = supabase.auth.currentUserOrNull()
            ?: return LoginResult.Failure("Login Failed")

        val user = repository.getUser(authUser.id)
            ?: return LoginResult.Failure("Profile not found")

        return LoginResult.Success(user)
    }
}