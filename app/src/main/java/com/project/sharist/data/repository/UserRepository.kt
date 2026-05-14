package com.project.sharist.data.repository

import com.project.sharist.data.local.UserDao
import com.project.sharist.domain.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun loginUser(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}