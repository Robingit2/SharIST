package com.project.sharist.data.usecase.auth

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth

class LogoutUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): GenericResult<Unit> {
        return safeSupabaseCall {
            supabase.auth.signOut()
            userRepository.clearCachedUsers()
        }
    }
}
