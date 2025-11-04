package com.project.swipetoplay.ui.features.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.repository.UserPreferenceRepository

class PreferencesViewModelFactory(
    private val userPreferenceRepository: UserPreferenceRepository = UserPreferenceRepository(),
    private val gameApiService: com.project.swipetoplay.data.remote.api.GameApiService = RetrofitClient.gameApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreferencesViewModel(userPreferenceRepository, gameApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

