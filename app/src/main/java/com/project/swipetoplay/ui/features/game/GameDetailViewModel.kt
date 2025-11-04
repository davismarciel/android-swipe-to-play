package com.project.swipetoplay.ui.features.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val game: GameResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for GameDetailScreen
 * Manages game details loading and display
 */
class GameDetailViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    /**
     * Load game details by ID
     */
    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = gameRepository.getGameById(gameId)

            result.fold(
                onSuccess = { gameResponse ->
                    Log.d("GameDetailViewModel", "=== GAME LOADED ===")
                    Log.d("GameDetailViewModel", "Game: ${gameResponse.name}")
                    Log.d("GameDetailViewModel", "Game ID: ${gameResponse.id}")
                    Log.d("GameDetailViewModel", "Community Rating: ${gameResponse.communityRating}")
                    gameResponse.communityRating?.let { rating ->
                        Log.d("GameDetailViewModel", "  Toxicity: ${rating.toxicity}")
                        Log.d("GameDetailViewModel", "  Bugs: ${rating.bugs}")
                        Log.d("GameDetailViewModel", "  Microtransactions: ${rating.microtransactions}")
                        Log.d("GameDetailViewModel", "  Optimization: ${rating.optimization}")
                        Log.d("GameDetailViewModel", "  Cheaters: ${rating.cheaters}")
                    }

                    _uiState.value = _uiState.value.copy(
                        game = gameResponse,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load game details"
                    )
                }
            )
        }
    }

    /**
     * Retry loading game details
     */
    fun retry() {
        _uiState.value.game?.id?.let { gameId ->
            loadGameDetails(gameId)
        }
    }
}

