package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class UserRepository {
    // TODO Remove GenericResult
    private val usersTable = supabase.postgrest["users"]
    private val userRolesTable = supabase.postgrest["roles"]

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
        val roleNames = userRolesTable.select(Columns.raw("roles (name)")) {
            filter {
                eq("user_id", userId)
            }
        } .decodeList<String>()

        return roleNames.mapNotNull { RoleType.from(it) }
    }

    suspend fun insert(user: User) : GenericResult<Unit> {
        return safeSupabaseCall {
            usersTable.insert(user)
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