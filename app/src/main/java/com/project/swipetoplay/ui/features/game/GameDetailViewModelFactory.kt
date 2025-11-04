package com.project.swipetoplay.ui.features.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.swipetoplay.data.repository.GameRepository

class GameDetailViewModelFactory(
    private val gameRepository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameDetailViewModel(gameRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

