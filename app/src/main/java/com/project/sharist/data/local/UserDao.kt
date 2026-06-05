package com.project.sharist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.sharist.data.model.user.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String): User?

    @Query("UPDATE users SET cacheLastAccessedAtMillis = :accessedAtMillis WHERE id = :userId")
    suspend fun updateLastAccessed(userId: String, accessedAtMillis: Long)

    @Query("""
        DELETE FROM users
        WHERE id NOT IN (
            SELECT id FROM users
            ORDER BY cacheLastAccessedAtMillis DESC
            LIMIT :limit
        )
    """)
    suspend fun trimToLimit(limit: Int)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
