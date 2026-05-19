package com.project.sharist.ui.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class HomeViewModel : ViewModel() {
    private val _destination = MutableStateFlow("")
    val destination: StateFlow<String> = _destination

    fun updateDestination(value: String) {
        _destination.update { value }
    }
}