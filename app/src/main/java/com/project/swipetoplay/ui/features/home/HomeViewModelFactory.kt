package com.project.swipetoplay.ui.features.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.swipetoplay.data.local.GameCacheManager
import com.project.swipetoplay.data.local.GameLimitManager
import com.project.swipetoplay.data.repository.InteractionRepository
import com.project.swipetoplay.data.repository.RecommendationRepository

class HomeViewModelFactory(
    private val recommendationRepository: RecommendationRepository,
    private val interactionRepository: InteractionRepository,
    private val gameLimitManager: GameLimitManager,
    private val gameCacheManager: GameCacheManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                recommendationRepository,
                interactionRepository,
                gameLimitManager,
                gameCacheManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

