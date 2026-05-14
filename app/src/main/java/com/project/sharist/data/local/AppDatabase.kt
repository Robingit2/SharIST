package com.project.sharist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.sharist.domain.model.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}