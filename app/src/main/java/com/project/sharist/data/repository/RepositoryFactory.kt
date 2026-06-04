package com.project.sharist.data.repository

import android.content.Context
import com.project.sharist.data.local.DatabaseProvider

fun cachedUserRepository(context: Context): UserRepository {
    return UserRepository(
        userDao = DatabaseProvider.getDatabase(context).userDao()
    )
}
