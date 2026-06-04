package com.project.sharist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.project.sharist.data.model.user.RoleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository (
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setActiveRole(userId: String, role: RoleType) {
        dataStore.edit {
            it[activeRoleKey(userId)] = role.name
        }
    }

    fun getActiveRole(userId: String): Flow<RoleType?> {
        return dataStore.data.map {
            it[activeRoleKey(userId)]?.let(RoleType::from)
        }
    }

    private fun activeRoleKey(userId: String): Preferences.Key<String> {
        return stringPreferencesKey("active_role_$userId")
    }
}
