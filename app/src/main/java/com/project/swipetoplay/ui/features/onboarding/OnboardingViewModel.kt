package com.project.swipetoplay.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.repository.UserPreferenceRepository
import com.project.swipetoplay.data.remote.dto.GenreResponse
import com.project.swipetoplay.data.remote.dto.CategoryResponse
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
    
    val toleranceMicrotransactions: Int = 5,
    val toleranceDlc: Int = 5,
    val toleranceLootBoxes: Int = 5,
    val toleranceBattlePass: Int = 5,
    val preferCosmeticOnly: Boolean = false,
    val avoidSubscription: Boolean = false,
    
    val isCompleted: Boolean = false
)

/**
 * ViewModel for Onboarding flow
 * Manages multi-step user preference configuration
 */
class OnboardingViewModel(
    private val gameRepository: GameRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var hasLoadedData = false

    /**
     * Load initial data from API
     * Called when the screen is displayed to ensure token is ready
     */
    fun loadInitialData() {
        if (hasLoadedData) return
        hasLoadedData = true
        
        android.util.Log.d("OnboardingViewModel", "📥 Loading initial data (genres & categories)")
        loadGenres()
        loadCategories()
    }

    /**
     * Load available genres from API
     */
    private fun loadGenres() {
        viewModelScope.launch {
            android.util.Log.d("OnboardingViewModel", "🎮 Fetching genres from API...")
            val result = gameRepository.getGenres()
            result.fold(
                onSuccess = { genres ->
                    android.util.Log.d("OnboardingViewModel", "✅ Loaded ${genres.size} genres")
                    _uiState.value = _uiState.value.copy(availableGenres = genres)
                },
                onFailure = { exception ->
                    android.util.Log.w("OnboardingViewModel", "⚠️ Failed to load genres: ${exception.message}")
                }
            )
        }
    }

    /**
     * Load available categories from API (for future use)
     */
    private fun loadCategories() {
        viewModelScope.launch {
            android.util.Log.d("OnboardingViewModel", "📂 Fetching categories from API...")
            gameRepository.getCategories()
                .fold(
                    onSuccess = { categories ->
                        android.util.Log.d("OnboardingViewModel", "✅ Loaded ${categories.size} categories")
                    },
                    onFailure = { exception ->
                        android.util.Log.w("OnboardingViewModel", "⚠️ Failed to load categories: ${exception.message}")
                    }
                )
        }
    }

    /**
     * Move to next step
     */
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps - 1) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
        }
    }

    /**
     * Move to previous step
     */
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1)
        }
    }

    /**
     * Toggle genre selection
     */
    fun toggleGenre(genreId: Int, weight: Int = 5) {
        val selected = _uiState.value.selectedGenres.toMutableMap()
        if (selected.containsKey(genreId)) {
            selected.remove(genreId)
        } else {
            selected[genreId] = weight
        }
        _uiState.value = _uiState.value.copy(selectedGenres = selected)
    }

    /**
     * Update genre weight
     */
    fun updateGenreWeight(genreId: Int, weight: Int) {
        val selected = _uiState.value.selectedGenres.toMutableMap()
        if (selected.containsKey(genreId)) {
            selected[genreId] = weight.coerceIn(1, 10)
            _uiState.value = _uiState.value.copy(selectedGenres = selected)
        }
    }

    /**
     * Save all preferences and complete onboarding
     */
    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                userPreferenceRepository.updatePreferences(
                    preferWindows = _uiState.value.preferWindows,
                    preferMac = _uiState.value.preferMac,
                    preferLinux = _uiState.value.preferLinux
                )

                userPreferenceRepository.updateMonetizationPreferences(
                    toleranceMicrotransactions = _uiState.value.toleranceMicrotransactions,
                    toleranceDlc = _uiState.value.toleranceDlc,
                    toleranceLootBoxes = _uiState.value.toleranceLootBoxes,
                    toleranceBattlePass = _uiState.value.toleranceBattlePass,
                    preferCosmeticOnly = _uiState.value.preferCosmeticOnly,
                    avoidSubscription = _uiState.value.avoidSubscription
                )

                val genresData = _uiState.value.selectedGenres.map { (genreId, weight) ->
                    com.project.swipetoplay.data.remote.dto.GenrePreferenceItem(
                        genreId = genreId,
                        preferenceWeight = weight
                    )
                }
                userPreferenceRepository.updatePreferredGenres(genresData)

                _uiState.value = _uiState.value.copy(isCompleted = true, isLoading = false)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save preferences"
                )
            }
        }
    }

    /**
     * Update platform preference
     */
    fun updatePlatformPreference(platform: String, enabled: Boolean) {
        _uiState.value = when (platform.lowercase()) {
            "windows" -> _uiState.value.copy(preferWindows = enabled)
            "mac" -> _uiState.value.copy(preferMac = enabled)
            "linux" -> _uiState.value.copy(preferLinux = enabled)
            else -> _uiState.value
        }
    }

    /**
     * Update monetization preference
     */
    fun updateMonetizationPreference(key: String, value: Any) {
        _uiState.value = when (key) {
            "tolerance_microtransactions" -> _uiState.value.copy(
                toleranceMicrotransactions = value as? Int ?: 5
            )
            "tolerance_dlc" -> _uiState.value.copy(
                toleranceDlc = value as? Int ?: 5
            )
            "tolerance_loot_boxes" -> _uiState.value.copy(
                toleranceLootBoxes = value as? Int ?: 5
            )
            "tolerance_battle_pass" -> _uiState.value.copy(
                toleranceBattlePass = value as? Int ?: 5
            )
            "prefer_cosmetic_only" -> _uiState.value.copy(
                preferCosmeticOnly = value as? Boolean ?: false
            )
            "avoid_subscription" -> _uiState.value.copy(
                avoidSubscription = value as? Boolean ?: false
            )
            else -> _uiState.value
        }
    }

    /**
     * Check if can proceed to next step
     */
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

