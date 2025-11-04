package com.project.swipetoplay.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.local.GameCacheManager
import com.project.swipetoplay.data.local.GameLimitManager
import com.project.swipetoplay.data.repository.InteractionRepository
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.domain.mapper.GameMapper
import com.project.swipetoplay.ui.features.game.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val games: List<Game> = emptyList(),
    val currentGameIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasReachedDailyLimit: Boolean = false,
    val remainingGames: Int = 20 // Default to 20 games per day
)

/**
 * ViewModel for HomeScreen
 * Manages game recommendations and swipe interactions
 */
class HomeViewModel(
    private val recommendationRepository: RecommendationRepository,
    private val interactionRepository: InteractionRepository,
    private val gameLimitManager: GameLimitManager,
    private val gameCacheManager: GameCacheManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateDailyLimitInfo()
            android.util.Log.d("HomeViewModel", "📊 Initialized - Remaining games: ${_uiState.value.remainingGames}, Current count: ${gameLimitManager.getCurrentCount()}, Limit: ${gameLimitManager.getDailyLimit()}")
        }
    }

    /**
     * Load game recommendations from API or cache
     * Checks cache first, then loads from API if cache is not available
     * Only loads if not already loading and hasn't reached daily limit
     */
    fun loadRecommendations() {
        if (_uiState.value.isLoading || _uiState.value.hasReachedDailyLimit) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Check cache first
            val cachedGames = gameCacheManager?.getCachedGames()
            if (cachedGames != null && cachedGames.isNotEmpty()) {
                android.util.Log.d("HomeViewModel", "✅ Using cached games: ${cachedGames.size} games")
                updateDailyLimitInfo()
                _uiState.value = _uiState.value.copy(
                    games = cachedGames,
                    currentGameIndex = 0,
                    isLoading = false,
                    error = null
                )
                return@launch
            }

            // If no cache, load from API
            android.util.Log.d("HomeViewModel", "🔄 Cache not available, loading from API...")
            val result = recommendationRepository.getRecommendations(limit = 10)

            result.fold(
                onSuccess = { gameResponses ->
                    val games = GameMapper.toGameList(gameResponses)
                    updateDailyLimitInfo()
                    _uiState.value = _uiState.value.copy(
                        games = games,
                        currentGameIndex = 0,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load games"
                    )
                }
            )
        }
    }

    /**
     * Handle swipe right (like) - send to API silently
     */
    fun onSwipeRight(game: Game) {
        viewModelScope.launch {
            interactionRepository.likeGame(game.id.toIntOrNull() ?: return@launch)
            
            gameLimitManager.incrementCount()
            updateDailyLimitInfo()
            
            android.util.Log.d("HomeViewModel", "👍 Like recorded - Remaining games: ${_uiState.value.remainingGames}, Current count: ${gameLimitManager.getCurrentCount()}, Limit: ${gameLimitManager.getDailyLimit()}")
        }
    }

    /**
     * Handle swipe left (dislike) - send to API silently
     * Both likes and dislikes count towards the daily limit
     */
    fun onSwipeLeft(game: Game) {
        viewModelScope.launch {
            interactionRepository.dislikeGame(game.id.toIntOrNull() ?: return@launch)
            
            gameLimitManager.incrementCount()
            updateDailyLimitInfo()
            
            android.util.Log.d("HomeViewModel", "👎 Dislike recorded - Remaining games: ${_uiState.value.remainingGames}, Current count: ${gameLimitManager.getCurrentCount()}, Limit: ${gameLimitManager.getDailyLimit()}")
        }
    }

    /**
     * Handle game view - record view only
     * Note: Does NOT increment daily count - only likes count
     */
    fun onGameViewed(game: Game) {
        viewModelScope.launch {
            interactionRepository.viewGame(game.id.toIntOrNull() ?: return@launch)
        }
    }

    /**
     * Move to next game in the list
     * NOTE: This does NOT increment the count - only likes increment the count
     */
    fun moveToNextGame() {
        val currentIndex = _uiState.value.currentGameIndex
        val games = _uiState.value.games
        
        if (currentIndex < games.size - 1) {
            _uiState.value = _uiState.value.copy(currentGameIndex = currentIndex + 1)
            updateDailyLimitInfo()
        } else {
            if (!_uiState.value.hasReachedDailyLimit) {
                loadRecommendations()
            }
        }
    }

    /**
     * Get current game
     */
    fun getCurrentGame(): Game? {
        val state = _uiState.value
        return if (state.currentGameIndex < state.games.size) {
            state.games[state.currentGameIndex]
        } else {
            null
        }
    }

    /**
     * Update daily limit information
     */
    private fun updateDailyLimitInfo() {
        val currentCount = gameLimitManager.getCurrentCount()
        val dailyLimit = gameLimitManager.getDailyLimit()
        val remaining = gameLimitManager.getRemainingGames()
        val hasReached = gameLimitManager.hasReachedLimit()
        
        android.util.Log.d("HomeViewModel", "🔄 Updating limit info - Count: $currentCount/$dailyLimit, Remaining: $remaining, HasReached: $hasReached")
        
        _uiState.value = _uiState.value.copy(
            hasReachedDailyLimit = hasReached,
            remainingGames = remaining
        )
    }

    /**
     * Retry loading recommendations
     */
    fun retry() {
        loadRecommendations()
    }
}

