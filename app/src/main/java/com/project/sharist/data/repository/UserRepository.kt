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
    private val usersTable = supabase.postgrest["users"]
    private val userRolesTable = supabase.postgrest["user_roles"]

    suspend fun getUser(userId: String): GenericResult<User> {
        return safeSupabaseCall {
            val now = System.currentTimeMillis()
            val cachedUser = userDao?.getUser(userId)
            if (cachedUser != null && now - cachedUser.cacheFetchedAtMillis < USER_CACHE_TTL_MILLIS) {
                userDao.updateLastAccessed(userId, now)
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

    suspend fun getUserRoles(userId: String): GenericResult<List<RoleType>> {
        return safeSupabaseCall {
            val roleRows = userRolesTable.select(Columns.raw("roles (name)")) {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<UserRoleNameRow>()

            roleRows.mapNotNull { RoleType.from(it.role.name) }
        }
    }

    suspend fun insert(user: User, userRoles: List<UserRole>): GenericResult<Unit> {
        return safeSupabaseCall {
            usersTable.insert(user)

            if (userRoles.isNotEmpty()) {
                userRolesTable.insert(userRoles)
            }
        }
    }

    suspend fun updateProfile(userId: String, update: UserProfileUpdate): GenericResult<Unit> {
        return safeSupabaseCall {
            usersTable.update(update) {
                filter {
                    eq("id", userId)
                }
            }

            refreshUser(userId)
        }
    }

    suspend fun clearCachedUsers(): GenericResult<Unit> {
        return safeSupabaseCall {
            userDao?.clearUsers()
        }
    }

    private suspend fun cacheUser(user: User) {
        val now = System.currentTimeMillis()
        userDao?.insertUser(
            user.copy(
                cacheLastAccessedAtMillis = now,
                cacheFetchedAtMillis = now
            )
        )
        userDao?.trimToLimit(USER_CACHE_LIMIT)
    }

    suspend fun uploadAvatar(context: Context, userId: String, uri: Uri): GenericResult<String> {
        return safeSupabaseCall {
            val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val path = "users/$userId/avatar.${contentType.toAvatarExtension()}"
            supabase.storage.from("avatars").upload(path, uri) {
                upsert = true
                this.contentType = ContentType.parse(contentType)
            }
            path
        }
    }

    suspend fun downloadAvatar(path: String): ByteArray {
        return supabase.storage.from("avatars").downloadAuthenticated(path)
    }
}

private const val USER_CACHE_LIMIT = 100
private const val USER_CACHE_TTL_MILLIS = 15 * 60 * 1000L

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
