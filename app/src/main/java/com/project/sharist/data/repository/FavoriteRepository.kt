package com.project.sharist.data.repository

import com.project.sharist.data.model.favorite.FavoriteLocationEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class FavoriteRepository(
    private val supabase: SupabaseClient
) {
    suspend fun addFavorite(
        userId: String,
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        supabase.from("favourite_starting_locations")
            .insert(
                FavoriteLocationEntity(
                    userId = userId,
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
            )
    }

    suspend fun getFavorites(userId: String): List<FavoriteLocationEntity> {
        return supabase.from("favourite_starting_locations")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList()
    }

    suspend fun removeFavorite(id: Long) {
        supabase.from("favourite_starting_locations")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}