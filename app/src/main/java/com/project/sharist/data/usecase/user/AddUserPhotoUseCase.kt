package com.project.sharist.data.usecase.user

import com.project.sharist.data.repository.UserRepository

class AddUserPhotoUseCase (
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        // TODO
    }
}
