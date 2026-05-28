package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.data.model.user.UserRole
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRepository {
    // TODO Remove GenericResult
    private val usersTable = supabase.postgrest["users"]
    private val userRolesTable = supabase.postgrest["user_roles"]

    suspend fun getUser(userId: String): GenericResult<User> {
        return safeSupabaseCall {
            usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
        }
    }

    suspend fun getUserRoles(userId: String): List<RoleType> {
        val roleRows = userRolesTable.select(Columns.raw("roles (name)")) {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<UserRoleNameRow>()

        return roleRows.mapNotNull { RoleType.from(it.role.name) }
    }

    suspend fun insert(user: User, userRoles: List<UserRole>) {
        usersTable.insert(user)

        if (userRoles.isNotEmpty()) {
            userRolesTable.insert(userRoles)
        }
    }

    // FIXME create a UpdateDto instead of using Any
    suspend fun update(userId: String, updates: Map<String, Any>) {
        usersTable.update (updates) {
            filter {
                eq("id", userId)
            }
        }
    }
}

@Serializable
private data class UserRoleNameRow(
    @SerialName("roles")
    val role: RoleName
)

@Serializable
private data class RoleName(
    val name: String
)
