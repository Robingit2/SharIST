package com.project.sharist.data.usecase.auth

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.auth.RegisterUserInput
import com.project.sharist.data.model.error.AuthException
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.model.user.User
import com.project.sharist.data.model.user.UserRole
import com.project.sharist.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class RegisterUserUseCase(
    private val repository: UserRepository
) {

    suspend operator fun invoke(data: RegisterUserInput) : GenericResult<Unit> {
        return safeSupabaseCall {
            supabase.auth.signUpWith(Email) {
                email = data.email
                password = data.password
            }

            val authUser = supabase.auth.currentUserOrNull()
                ?: throw AuthException(
                    "Account was created, but Supabase did not return a logged in user"
                )

            val newUser = User(
                id = authUser.id,
                name = data.name,
                photoPath = data.photoPath
            )

            val userRoles = data.roles.mapNotNull { role ->
                RoleType.from(role)?.let { roleType ->
                    UserRole(
                        userId = authUser.id,
                        roleId = roleType.id
                    )
                }
            }

            repository.insert(newUser, userRoles)
        }
    }
}
