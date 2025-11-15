package com.project.swipetoplay.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.swipetoplay.data.repository.InteractionRepository
import com.project.swipetoplay.data.repository.RecommendationRepository

class HomeViewModelFactory(
    private val recommendationRepository: RecommendationRepository,
    private val interactionRepository: InteractionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                recommendationRepository,
                interactionRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

