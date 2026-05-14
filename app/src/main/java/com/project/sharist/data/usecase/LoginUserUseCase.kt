package com.project.sharist.data.usecase

import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.model.User
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class LoginUserUseCase(private val repository: UserRepository) {

    suspend operator fun invoke(data: LoginUserInput): User? {
        supabase.auth.signInWith(Email) {
            email = data.email
            password = data.password
        }


        val authUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("Login Failed")

        val user = repository.getUser(authUser.id)
            ?: throw IllegalStateException("Profile not found")

        return user
    }
}