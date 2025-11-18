package com.project.swipetoplay.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.repository.InteractionRepository
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.domain.mapper.GameMapper
import com.project.swipetoplay.ui.features.game.Game
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val games: List<Game> = emptyList(),
    val currentGameIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val recommendationRepository: RecommendationRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadRecommendations() {
        val state = _uiState.value
        if (state.isLoading) {
            ErrorLogger.logDebug("HomeViewModel", "Skipping load - already loading")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            ErrorLogger.logDebug("HomeViewModel", "Loading recommendations from API")
            val result = recommendationRepository.getRecommendations(limit = 20)

            result.fold(
                onSuccess = { response ->
                    val games = GameMapper.toGameList(response.recommendations)

                    ErrorLogger.logDebug(
                        "HomeViewModel",
                        "Loaded ${games.size} games"
                    )

                    _uiState.value = HomeUiState(
                        games = games,
                        currentGameIndex = 0,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    ErrorLogger.logError("HomeViewModel", "Failed to load games: ${exception.message}", exception)
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = ErrorHandler.getUserFriendlyMessage(exception)
                    )
                }
            )
        }
    }

    fun onSwipeRight(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            val result = interactionRepository.likeGame(gameId)

            result.fold(
                onSuccess = {
                    ErrorLogger.logDebug("HomeViewModel", "Like recorded for game $gameId")
                },
                onFailure = { error ->
                    ErrorLogger.logError("HomeViewModel", "Failed to record like: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(error = ErrorHandler.getUserFriendlyMessage(error))
                }
            )
        }
    }

    fun onSwipeLeft(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            val result = interactionRepository.dislikeGame(gameId)

            result.fold(
                onSuccess = {
                    ErrorLogger.logDebug("HomeViewModel", "Dislike recorded for game $gameId")
                },
                onFailure = { error ->
                    ErrorLogger.logError("HomeViewModel", "Failed to record dislike: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(error = ErrorHandler.getUserFriendlyMessage(error))
                }
            )
        }
    }

    fun onGameViewed(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            interactionRepository.viewGame(gameId)
        }
    }

    fun moveToNextGame() {
        val currentState = _uiState.value
        val currentIndex = currentState.currentGameIndex
        val games = currentState.games

        if (currentIndex < games.size - 1) {
            _uiState.value = currentState.copy(currentGameIndex = currentIndex + 1)
        } else {
            loadRecommendations()
        }
    }

    fun getCurrentGame(): Game? {
        val state = _uiState.value
        return state.games.getOrNull(state.currentGameIndex)
    }

    fun retry() {
        loadRecommendations()
    }

    fun forceReload() {
        _uiState.value = HomeUiState()
        loadRecommendations()
    }
}

