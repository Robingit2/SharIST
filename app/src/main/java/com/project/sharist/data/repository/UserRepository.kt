package com.project.sharist.data.repository

import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.User
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest

class UserRepository {
    private val usersTable = supabase.postgrest["users"]

    suspend fun getUser(userId: String): GenericResult<User> {
        return safeSupabaseCall {
            usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
        }
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