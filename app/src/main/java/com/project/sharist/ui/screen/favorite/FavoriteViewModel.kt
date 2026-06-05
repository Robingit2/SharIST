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
import kotlinx.coroutines.flow.StateFlow

enum class FavoriteDisplayMode {
    NONE,
    SINGLE,
    ALL
}
class FavoriteViewModel : ViewModel() {

    private val repository = FavoriteRepository(supabase)
    private val _favorites = MutableStateFlow<List<FavoriteLocationEntity>>(emptyList())
    val favorites = _favorites.asStateFlow()
    private val _selectedLocation = MutableStateFlow<FavoriteLocationEntity?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()
    private val _displayMode = MutableStateFlow(FavoriteDisplayMode.ALL)
    val displayMode: StateFlow<FavoriteDisplayMode> = _displayMode


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

    fun showSingleFavorite(favorite: FavoriteLocationEntity) {
        _selectedLocation.value = favorite
        _displayMode.value = FavoriteDisplayMode.SINGLE
    }
    fun showAllFavorites() {
        _displayMode.value = FavoriteDisplayMode.ALL
    }

    fun setShowAllFavorites(show: Boolean) {
        _displayMode.value = if (show) FavoriteDisplayMode.ALL else FavoriteDisplayMode.NONE
    }

    fun clearFavoritesFromMap() {
        _displayMode.value = FavoriteDisplayMode.NONE
    }

}
