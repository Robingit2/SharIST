package com.project.sharist.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.data.model.favorite.FavoriteLocationEntity
import com.project.sharist.data.repository.FavoriteRepository
import com.project.sharist.supabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
class FavoriteViewModel : ViewModel() {

    private val repository = FavoriteRepository(supabase)
    private val _favorites = MutableStateFlow<List<FavoriteLocationEntity>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private val _selectedLocation = MutableStateFlow<FavoriteLocationEntity?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    fun selectFavorite(location: FavoriteLocationEntity) {
        _selectedLocation.value = location
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            runCatching {
                repository.getFavorites(userId)
            }.onSuccess {
                _favorites.value = it
            }
        }
    }

    fun addFavorite(userId: String, name: String, lat: Double, lng: Double) {
        Log.e("FAV_INSERT", "ENTERED addFavorite FUNCTION")

        viewModelScope.launch {
            runCatching {
                repository.addFavorite(userId, name, lat, lng)
            }.onSuccess {
                loadFavorites(userId) //  REFRESH STATE
            }.onFailure {
                Log.e("FAV_INSERT", "Insert failed", it)
            }
        }
    }

    fun removeFavorite(userId: String,id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.removeFavorite(id)
                repository.getFavorites(userId)
            }.onSuccess {
                _favorites.value = it
            }
        }
    }
}