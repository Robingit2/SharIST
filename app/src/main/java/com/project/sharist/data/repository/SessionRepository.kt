package com.project.sharist.data.repository

import android.content.Context
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
    companion object {
        val ACTIVE_ROLE = stringPreferencesKey("active_role")
    }

    suspend fun setActiveRole(role: RoleType) {
        dataStore.edit {
            it[ACTIVE_ROLE] = role.name
        }
    }

    fun getActiveRole(): Flow<RoleType?> {
        return dataStore.data.map {
            it[ACTIVE_ROLE]?.let(RoleType::from)
        }
    }
}