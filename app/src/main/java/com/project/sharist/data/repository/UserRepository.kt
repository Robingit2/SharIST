package com.project.sharist.data.repository

import android.content.Context
import android.net.Uri
import com.project.sharist.data.local.UserDao
import com.project.sharist.data.model.GenericResult
import com.project.sharist.data.model.helpers.safeSupabaseCall
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.model.user.User
import com.project.sharist.data.model.user.UserRole
import com.project.sharist.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRepository(
    private val userDao: UserDao? = null
) {
    // TODO Remove GenericResult
    private val usersTable = supabase.postgrest["users"]
    private val userRolesTable = supabase.postgrest["user_roles"]

    suspend fun getUser(userId: String): GenericResult<User> {
        return safeSupabaseCall {
            val cachedUser = userDao?.getUser(userId)
            if (cachedUser != null) {
                userDao.updateLastAccessed(userId, System.currentTimeMillis())
                return@safeSupabaseCall cachedUser
            }

            usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
                .also { user -> cacheUser(user) }
        }
    }

    suspend fun refreshUser(userId: String): GenericResult<User> {
        return safeSupabaseCall {
            usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
                .also { user -> cacheUser(user) }
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

    suspend fun updateProfile(userId: String, update: UserProfileUpdate) {
        usersTable.update(update) {
            filter {
                eq("id", userId)
            }
        }

        refreshUser(userId)
    }

    suspend fun clearCachedUsers() {
        userDao?.clearUsers()
    }

    private suspend fun cacheUser(user: User) {
        userDao?.insertUser(
            user.copy(cacheLastAccessedAtMillis = System.currentTimeMillis())
        )
        userDao?.trimToLimit(USER_CACHE_LIMIT)
    }

    suspend fun uploadAvatar(context: Context, userId: String, uri: Uri): String {
        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val path = "users/$userId/avatar.${contentType.toAvatarExtension()}"
        supabase.storage.from("avatars").upload(path, uri) {
            upsert = true
            this.contentType = ContentType.parse(contentType)
        }
        return path
    }

    suspend fun downloadAvatar(path: String): ByteArray {
        return supabase.storage.from("avatars").downloadAuthenticated(path)
    }
}

private const val USER_CACHE_LIMIT = 100

private fun String.toAvatarExtension(): String {
    return when (lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
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

@Serializable
data class UserProfileUpdate(
    val name: String,
    @SerialName("photo_path")
    val photoPath: String? = null
)
