package com.project.sharist.data.usecase.auth

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.auth.LoginUserInput
import com.project.sharist.data.model.error.AuthException
import com.project.sharist.data.model.error.NotFoundException
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.User
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class LoginUserUseCase(private val repository: UserRepository) {

    suspend operator fun invoke(data: LoginUserInput): GenericResult<User> {
        return safeSupabaseCall {
            supabase.auth.signInWith(Email) {
                email = data.email
                password = data.password
            }

            val authUser = supabase.auth.currentUserOrNull()
                ?: throw AuthException()

            val user = when (val result = repository.getUser(authUser.id)) {
                is GenericResult.Success -> result.data
                is GenericResult.Error -> throw NotFoundException()
            }

            user
        }
    }
}