package com.project.sharist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    suspend fun setUserRoles(userId: String, roles: List<RoleType>) {
        dataStore.edit {
            it[userRolesKey(userId)] = roles.map { role -> role.name }.toSet()
        }
    }

    fun getUserRoles(userId: String): Flow<List<RoleType>> {
        return dataStore.data.map {
            it[userRolesKey(userId)]
                .orEmpty()
                .mapNotNull(RoleType::from)
        }
    }

    private fun activeRoleKey(userId: String): Preferences.Key<String> {
        return stringPreferencesKey("active_role_$userId")
    }

    private fun userRolesKey(userId: String): Preferences.Key<Set<String>> {
        return stringSetPreferencesKey("user_roles_$userId")
    }
}
