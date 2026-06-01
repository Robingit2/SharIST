package com.project.sharist.ui.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.sharist.domain.model.LatLng
import kotlinx.coroutines.launch

fun ViewModel.launchAddressSearch(
    context: Context,
    query: String,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onFound: (LatLng) -> Unit
) {
    if (query.isBlank()) {
        onError("Enter an address to search.")
        return
    }

    viewModelScope.launch {
        onLoadingChange(true)
        onError(null)

        try {
            val location = findAddressCoordinates(context, query)

            if (location == null) {
                onLoadingChange(false)
                onError("Address not found.")
            } else {
                onFound(location)
                onLoadingChange(false)
            }
        } catch (exception: Exception) {
            onLoadingChange(false)
            onError(exception.message ?: "Could not search address.")
        }
    }
}
