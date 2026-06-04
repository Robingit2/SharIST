package com.project.sharist.data.usecase.auth

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.repository.RideOfferRepository
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth

class LogoutUserUseCase(
    private val userRepository: UserRepository,
    private val rideOfferRepository: RideOfferRepository
) {
    suspend operator fun invoke(): GenericResult<Unit> {
        val signOutResult = safeSupabaseCall {
            supabase.auth.signOut()
        }

        if (signOutResult is GenericResult.Error) return signOutResult

        when (val result = userRepository.clearCachedUsers()) {
            is GenericResult.Success -> Unit
            is GenericResult.Error -> return result
        }

        return rideOfferRepository.clearCachedOffers()
    }
}
