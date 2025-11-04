package com.project.swipetoplay.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.repository.UserPreferenceRepository

class OnboardingViewModelFactory(
    private val gameRepository: GameRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(gameRepository, userPreferenceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

