package com.project.swipetoplay.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.remote.dto.DailyLimitInfoResponse
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

data class DailyLimitInfoUi(
    val currentCount: Int = 0,
    val dailyLimit: Int = 20,
    val remainingToday: Int = 20,
    val limitReached: Boolean = false
)

data class HomeUiState(
    val games: List<Game> = emptyList(),
    val currentGameIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasReachedDailyLimit: Boolean = false,
    val remainingGames: Int = 20,
    val dailyLimitInfo: DailyLimitInfoUi = DailyLimitInfoUi()
)

/**
 * ViewModel for HomeScreen
 * Manages game recommendations and swipe interactions relying on backend limits
 */
class HomeViewModel(
    private val recommendationRepository: RecommendationRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Load game recommendations from API
     * Backend is the single source of truth for limits
     */
    fun loadRecommendations() {
        val state = _uiState.value
        if (state.isLoading || state.hasReachedDailyLimit) {
            ErrorLogger.logDebug("HomeViewModel", "Skipping load - isLoading=${state.isLoading}, limitReached=${state.hasReachedDailyLimit}")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            ErrorLogger.logDebug("HomeViewModel", "Loading recommendations from API (backend authoritative)")
            val result = recommendationRepository.getRecommendations(limit = 20)

            result.fold(
                onSuccess = { response ->
                    val games = GameMapper.toGameList(response.recommendations)
                    val limitInfo = response.dailyLimitInfo?.toUi()
                        ?: DailyLimitInfoUi(
                            currentCount = response.count,
                            dailyLimit = response.limit,
                            remainingToday = (response.limit - response.count).coerceAtLeast(0),
                            limitReached = (response.limit - response.count) <= 0
                        )

                    ErrorLogger.logDebug(
                        "HomeViewModel",
                        "Loaded ${games.size} games - remaining today: ${limitInfo.remainingToday}/${limitInfo.dailyLimit}"
                    )

                    val message = if (limitInfo.limitReached && !response.message.isNullOrBlank()) {
                        response.message
                    } else {
                        null
                    }

                    val remainingGames = if (games.isEmpty() && !limitInfo.limitReached) {
                        0
                    } else {
                        limitInfo.remainingToday
                    }

                    _uiState.value = HomeUiState(
                        games = games,
                        currentGameIndex = 0,
                        isLoading = false,
                        error = message,
                        hasReachedDailyLimit = limitInfo.limitReached,
                        remainingGames = remainingGames,
                        dailyLimitInfo = limitInfo
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

    /**
     * Handle swipe right (like)
     */
    fun onSwipeRight(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            val result = interactionRepository.likeGame(gameId)

            result.fold(
                onSuccess = {
                    ErrorLogger.logDebug("HomeViewModel", "Like recorded for game $gameId")
                    decrementDailyLimit()
                },
                onFailure = { error ->
                    ErrorLogger.logError("HomeViewModel", "Failed to record like: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(error = ErrorHandler.getUserFriendlyMessage(error))
                }
            )
        }
    }

    /**
     * Handle swipe left (dislike)
     */
    fun onSwipeLeft(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            val result = interactionRepository.dislikeGame(gameId)

            result.fold(
                onSuccess = {
                    ErrorLogger.logDebug("HomeViewModel", "Dislike recorded for game $gameId")
                    decrementDailyLimit()
                },
                onFailure = { error ->
                    ErrorLogger.logError("HomeViewModel", "Failed to record dislike: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(error = ErrorHandler.getUserFriendlyMessage(error))
                }
            )
        }
    }

    /**
     * Handle game view - register without affecting limits
     */
    fun onGameViewed(game: Game) {
        val gameId = game.id.toIntOrNull() ?: return

        viewModelScope.launch {
            interactionRepository.viewGame(gameId)
        }
    }

    /**
     * Move to next game in the list
     */
    fun moveToNextGame() {
        val currentState = _uiState.value
        val currentIndex = currentState.currentGameIndex
        val games = currentState.games

        if (currentIndex < games.size - 1) {
            _uiState.value = currentState.copy(currentGameIndex = currentIndex + 1)
        } else if (!currentState.hasReachedDailyLimit) {
            loadRecommendations()
        }
    }

    /**
     * Get current game
     */
    fun getCurrentGame(): Game? {
        val state = _uiState.value
        return state.games.getOrNull(state.currentGameIndex)
    }

    /**
     * Retry loading recommendations
     */
    fun retry() {
        loadRecommendations()
    }

    /**
     * Force reload recommendations
     */
    fun forceReload() {
        _uiState.value = HomeUiState()
        loadRecommendations()
    }

    private fun decrementDailyLimit() {
        val state = _uiState.value
        val info = state.dailyLimitInfo
        if (info.limitReached) {
            return
        }

        val updatedInfo = info.copy(
            currentCount = (info.currentCount + 1).coerceAtMost(info.dailyLimit),
            remainingToday = (info.remainingToday - 1).coerceAtLeast(0)
        ).let { updated ->
            updated.copy(limitReached = updated.remainingToday <= 0)
        }

        _uiState.value = state.copy(
            dailyLimitInfo = updatedInfo,
            remainingGames = updatedInfo.remainingToday,
            hasReachedDailyLimit = updatedInfo.limitReached
        )
    }

    private fun DailyLimitInfoResponse.toUi(): DailyLimitInfoUi {
        return DailyLimitInfoUi(
            currentCount = currentCount,
            dailyLimit = dailyLimit,
            remainingToday = remainingToday,
            limitReached = limitReached
        )
    }
}

