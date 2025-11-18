package com.project.swipetoplay.ui.features.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val game: GameResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)


class GameDetailViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    
    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = gameRepository.getGameById(gameId)

            result.fold(
                onSuccess = { gameResponse ->
                    ErrorLogger.logDebug("GameDetailViewModel", "Game loaded: ${gameResponse.name} (ID: ${gameResponse.id})")

                    _uiState.value = _uiState.value.copy(
                        game = gameResponse,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    ErrorLogger.logError("GameDetailViewModel", "Failed to load game details: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorHandler.getUserFriendlyMessage(exception)
                    )
                }
            )
        }
    }

    
    fun retry() {
        _uiState.value.game?.id?.let { gameId ->
            loadGameDetails(gameId)
        }
    }
}

