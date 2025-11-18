package com.project.swipetoplay.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.repository.OnboardingRepository
import com.project.swipetoplay.data.remote.dto.GenreResponse
import com.project.swipetoplay.data.remote.dto.CategoryResponse
import com.project.swipetoplay.data.remote.dto.OnboardingCompleteRequest
import com.project.swipetoplay.data.remote.dto.PreferencesData
import com.project.swipetoplay.data.remote.dto.MonetizationData
import com.project.swipetoplay.data.remote.dto.GenrePreferenceItem
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 3,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    val availableGenres: List<GenreResponse> = emptyList(),
    val selectedGenres: Map<Int, Int> = emptyMap(),
    
    val preferWindows: Boolean = false,
    val preferMac: Boolean = false,
    val preferLinux: Boolean = false,
    
    val toleranceToxicity: Int = 5,
    val toleranceBugs: Int = 5,
    val toleranceMicrotransactions: Int = 5,
    val toleranceOptimization: Int = 5,
    val toleranceCheaters: Int = 5,
    
    val isCompleted: Boolean = false
)


class OnboardingViewModel(
    private val gameRepository: GameRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var hasLoadedData = false

    
    fun loadInitialData() {
        if (hasLoadedData) return
        hasLoadedData = true
        
        ErrorLogger.logDebug("OnboardingViewModel", "Loading initial data (genres & categories)")
        loadGenres()
        loadCategories()
    }

    
    private fun loadGenres() {
        viewModelScope.launch {
            ErrorLogger.logDebug("OnboardingViewModel", "Fetching genres from API")
            val result = gameRepository.getGenres()
            result.fold(
                onSuccess = { genres ->
                    ErrorLogger.logDebug("OnboardingViewModel", "Loaded ${genres.size} genres")
                    _uiState.value = _uiState.value.copy(availableGenres = genres)
                },
                onFailure = { exception ->
                    ErrorLogger.logWarning("OnboardingViewModel", "Failed to load genres: ${exception.message}", exception)
                }
            )
        }
    }

    
    private fun loadCategories() {
        viewModelScope.launch {
            ErrorLogger.logDebug("OnboardingViewModel", "Fetching categories from API")
            gameRepository.getCategories()
                .fold(
                    onSuccess = { categories ->
                        ErrorLogger.logDebug("OnboardingViewModel", "Loaded ${categories.size} categories")
                    },
                    onFailure = { exception ->
                        ErrorLogger.logWarning("OnboardingViewModel", "Failed to load categories: ${exception.message}", exception)
                    }
                )
        }
    }

    
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps - 1) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
        }
    }

    
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1)
        }
    }

    
    fun toggleGenre(genreId: Int, weight: Int = 5) {
        val selected = _uiState.value.selectedGenres.toMutableMap()
        if (selected.containsKey(genreId)) {
            selected.remove(genreId)
        } else {
            selected[genreId] = weight
        }
        _uiState.value = _uiState.value.copy(selectedGenres = selected)
    }

    
    fun updateGenreWeight(genreId: Int, weight: Int) {
        val selected = _uiState.value.selectedGenres.toMutableMap()
        if (selected.containsKey(genreId)) {
            selected[genreId] = weight.coerceIn(1, 10)
            _uiState.value = _uiState.value.copy(selectedGenres = selected)
        }
    }

    
    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                ErrorLogger.logDebug("OnboardingViewModel", "Completing onboarding")
                
                val preferencesData = PreferencesData(
                    preferWindows = _uiState.value.preferWindows,
                    preferMac = _uiState.value.preferMac,
                    preferLinux = _uiState.value.preferLinux
                )

                val monetizationData = MonetizationData(
                    toleranceMicrotransactions = _uiState.value.toleranceMicrotransactions,
                    toleranceDlc = _uiState.value.toleranceBugs, // Map bugs to DLC tolerance
                    toleranceLootBoxes = _uiState.value.toleranceToxicity, // Map toxicity to loot boxes
                    tolerancePayToWin = _uiState.value.toleranceCheaters, // Map cheaters to pay-to-win
                    toleranceBattlePass = _uiState.value.toleranceOptimization // Map optimization to battle pass
                )

                val genresList = _uiState.value.selectedGenres.map { (genreId, weight) ->
                    GenrePreferenceItem(
                        genreId = genreId,
                        preferenceWeight = weight
                    )
                }

                val request = OnboardingCompleteRequest(
                    preferences = preferencesData,
                    monetization = monetizationData,
                    genres = genresList,
                    categories = null // Not collecting categories in current onboarding flow
                )

                val result = onboardingRepository.completeOnboarding(request)
                
                result.fold(
                    onSuccess = { data ->
                        ErrorLogger.logDebug("OnboardingViewModel", "Onboarding completed successfully")
                        _uiState.value = _uiState.value.copy(isCompleted = true, isLoading = false)
                        onComplete()
                    },
                    onFailure = { exception ->
                        ErrorLogger.logError("OnboardingViewModel", "Failed to complete onboarding: ${exception.message}", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = ErrorHandler.getUserFriendlyMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                ErrorLogger.logError("OnboardingViewModel", "Exception during onboarding: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ErrorHandler.getUserFriendlyMessage(e)
                )
            }
        }
    }

    
    fun updatePlatformPreference(platform: String, enabled: Boolean) {
        _uiState.value = when (platform.lowercase()) {
            "windows" -> _uiState.value.copy(preferWindows = enabled)
            "mac" -> _uiState.value.copy(preferMac = enabled)
            "linux" -> _uiState.value.copy(preferLinux = enabled)
            else -> _uiState.value
        }
    }

    
    fun updateRatingTolerance(key: String, value: Int) {
        _uiState.value = when (key) {
            "toxicity" -> _uiState.value.copy(
                toleranceToxicity = value.coerceIn(0, 10)
            )
            "bugs" -> _uiState.value.copy(
                toleranceBugs = value.coerceIn(0, 10)
            )
            "microtransactions" -> _uiState.value.copy(
                toleranceMicrotransactions = value.coerceIn(0, 10)
            )
            "optimization" -> _uiState.value.copy(
                toleranceOptimization = value.coerceIn(0, 10)
            )
            "cheaters" -> _uiState.value.copy(
                toleranceCheaters = value.coerceIn(0, 10)
            )
            else -> _uiState.value
        }
    }

    
    fun canProceedToNext(): Boolean {
        val state = _uiState.value
        return when (state.currentStep) {
            0 -> state.selectedGenres.isNotEmpty()
            1 -> state.preferWindows || state.preferMac || state.preferLinux
            2 -> true
            else -> true
        }
    }
}

