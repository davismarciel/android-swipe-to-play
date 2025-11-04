package com.project.swipetoplay.ui.features.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.GenreResponse
import com.project.swipetoplay.data.remote.dto.CategoryResponse
import com.project.swipetoplay.data.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

data class PreferencesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    
    val availableGenres: List<GenreResponse> = emptyList(),
    val availableCategories: List<CategoryResponse> = emptyList(),
    
    val windowsSelected: Boolean = false,
    val macSelected: Boolean = false,
    val linuxSelected: Boolean = false,
    
    val selectedGenres: Set<Int> = emptySet(),
    
    val selectedCategories: Set<Int> = emptySet(),
    
    val casualSelected: Boolean = false,
    val competitiveSelected: Boolean = false,
    val storyDrivenSelected: Boolean = false,
    
    val freeToPlaySelected: Boolean = false,
    val paidSelected: Boolean = false,
    val subscriptionSelected: Boolean = false
)

/**
 * ViewModel for PreferencesScreen
 * Manages user preferences and integrates with backend API
 */
class PreferencesViewModel(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val gameApiService: com.project.swipetoplay.data.remote.api.GameApiService = RetrofitClient.gameApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    private val genreNameToId: MutableMap<String, Int> = mutableMapOf()
    
    private val categoryNameToId: MutableMap<String, Int> = mutableMapOf()
    
    private var savedState: PreferencesUiState? = null
    
    // Cache to avoid loading genres multiple times
    private var genresLoaded = false

    init {
        loadPreferencesData()
    }
    
    private fun checkForUnsavedChanges() {
        val current = _uiState.value
        val saved = savedState
        
        if (saved == null) {
            _uiState.value = current.copy(hasUnsavedChanges = false)
            return
        }
        
        val hasChanges = current.windowsSelected != saved.windowsSelected ||
                current.macSelected != saved.macSelected ||
                current.linuxSelected != saved.linuxSelected ||
                current.selectedGenres != saved.selectedGenres ||
                current.selectedCategories != saved.selectedCategories ||
                current.casualSelected != saved.casualSelected ||
                current.competitiveSelected != saved.competitiveSelected ||
                current.storyDrivenSelected != saved.storyDrivenSelected ||
                current.freeToPlaySelected != saved.freeToPlaySelected ||
                current.paidSelected != saved.paidSelected ||
                current.subscriptionSelected != saved.subscriptionSelected
        
        _uiState.value = current.copy(hasUnsavedChanges = hasChanges)
    }

    /**
     * Load available genres and current user preferences
     * Optimized: loads genres and preferences in parallel, skips categories
     */
    private fun loadPreferencesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load genres and user preferences in parallel for better performance
                val genresDeferred = async { 
                    if (!genresLoaded || _uiState.value.availableGenres.isEmpty()) {
                        loadGenres()
                    } else {
                        Result.success(_uiState.value.availableGenres)
                    }
                }
                
                val preferencesDeferred = async { 
                    loadUserPreferences()
                }
                
                // Wait for both to complete in parallel
                val genresResult = genresDeferred.await()
                preferencesDeferred.await()
                
                if (genresResult.isFailure) {
                    val errorMessage = genresResult.exceptionOrNull()?.message ?: "Failed to load genres"
                    android.util.Log.e("PreferencesViewModel", "Failed to load genres: $errorMessage")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load genres: $errorMessage"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("PreferencesViewModel", "Error loading preferences data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load preferences: ${e.message}"
                )
            }
        }
    }

    /**
     * Load genres from backend and build name-to-ID mapping
     * Uses cache to avoid unnecessary API calls
     */
    private suspend fun loadGenres(): Result<List<GenreResponse>> {
        return try {
            android.util.Log.d("PreferencesViewModel", "🎮 Starting to load genres from API...")
            val response = gameApiService.getGenres()
            android.util.Log.d("PreferencesViewModel", "📡 API response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("PreferencesViewModel", "📦 Response body - success: ${body?.success}, data: ${body?.data?.size} genres")
                
                if (body?.success == true && body.data != null) {
                    val genres = body.data
                    
                    if (genres.isEmpty()) {
                        android.util.Log.w("PreferencesViewModel", "⚠️ Received empty genres list")
                        return Result.failure(Exception("No genres available"))
                    }
                    
                    genreNameToId.clear()
                    genres.forEach { genre ->
                        genreNameToId[genre.name.lowercase()] = genre.id
                    }
                    
                    _uiState.value = _uiState.value.copy(availableGenres = genres)
                    genresLoaded = true // Mark as loaded to use cache next time
                    android.util.Log.d("PreferencesViewModel", "✅ Successfully loaded ${genres.size} genres: ${genres.map { it.name }}")
                    Result.success(genres)
                } else {
                    val errorMsg = body?.message ?: "Failed to fetch genres"
                    android.util.Log.e("PreferencesViewModel", "❌ API returned unsuccessful response: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                android.util.Log.e("PreferencesViewModel", "❌ HTTP error: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesViewModel", "❌ Exception while loading genres", e)
            Result.failure(e)
        }
    }

    /**
     * Load categories from backend and build name-to-ID mapping
     */
    private suspend fun loadCategories(): Result<List<CategoryResponse>> {
        return try {
            val response = gameApiService.getCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val categories = body.data
                    
                    categoryNameToId.clear()
                    categories.forEach { category ->
                        categoryNameToId[category.name.lowercase()] = category.id
                    }
                    
                    _uiState.value = _uiState.value.copy(availableCategories = categories)
                    android.util.Log.d("PreferencesViewModel", "Loaded ${categories.size} categories")
                    Result.success(categories)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch categories"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load current user preferences from backend
     */
    private suspend fun loadUserPreferences() {
        try {
            val result = userPreferenceRepository.getPreferences()
            result.onSuccess { preferences ->
                android.util.Log.d("PreferencesViewModel", "Loaded user preferences")
                
                val currentState = _uiState.value
                val newState = currentState.copy(
                    windowsSelected = preferences.preferences?.preferWindows == true,
                    macSelected = preferences.preferences?.preferMac == true,
                    linuxSelected = preferences.preferences?.preferLinux == true,
                    
                    selectedGenres = preferences.preferredGenres?.mapNotNull { it.id }?.toSet() ?: emptySet(),
                    
                    selectedCategories = preferences.preferredCategories?.mapNotNull { it.id }?.toSet() ?: emptySet(),
                    
                    casualSelected = false,
                    competitiveSelected = preferences.preferences?.preferCompetitive == true,
                    storyDrivenSelected = false,
                    
                    freeToPlaySelected = preferences.preferences?.preferFreeToPlay == true,
                    paidSelected = preferences.monetizationPreferences?.preferOneTimePurchase == true,
                    subscriptionSelected = preferences.monetizationPreferences?.avoidSubscription == false,
                    hasUnsavedChanges = false
                )
                _uiState.value = newState
                savedState = newState.copy(hasUnsavedChanges = false)
            }.onFailure { error ->
                android.util.Log.w("PreferencesViewModel", "Failed to load preferences: ${error.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesViewModel", "Error loading user preferences", e)
        }
    }

    /**
     * Toggle platform selection
     */
    fun togglePlatform(platform: String) {
        when (platform.lowercase()) {
            "windows" -> _uiState.value = _uiState.value.copy(windowsSelected = !_uiState.value.windowsSelected)
            "mac" -> _uiState.value = _uiState.value.copy(macSelected = !_uiState.value.macSelected)
            "linux" -> _uiState.value = _uiState.value.copy(linuxSelected = !_uiState.value.linuxSelected)
        }
        checkForUnsavedChanges()
    }

    /**
     * Toggle genre selection by name (UI tag)
     */
    fun toggleGenre(genreName: String) {
        val genreId = genreNameToId[genreName.lowercase()]
        if (genreId != null) {
            val currentSelected = _uiState.value.selectedGenres
            val newSelected = if (currentSelected.contains(genreId)) {
                currentSelected - genreId
            } else {
                currentSelected + genreId
            }
            _uiState.value = _uiState.value.copy(selectedGenres = newSelected)
            checkForUnsavedChanges()
        } else {
            android.util.Log.w("PreferencesViewModel", "Genre not found: $genreName")
        }
    }

    /**
     * Check if genre is selected by name
     */
    fun isGenreSelected(genreName: String): Boolean {
        val genreId = genreNameToId[genreName.lowercase()]
        return genreId != null && _uiState.value.selectedGenres.contains(genreId)
    }

    /**
     * Toggle category selection by name (UI tag)
     */
    fun toggleCategory(categoryName: String) {
        val categoryId = categoryNameToId[categoryName.lowercase()]
        if (categoryId != null) {
            val currentSelected = _uiState.value.selectedCategories
            val newSelected = if (currentSelected.contains(categoryId)) {
                currentSelected - categoryId
            } else {
                currentSelected + categoryId
            }
            _uiState.value = _uiState.value.copy(selectedCategories = newSelected)
            checkForUnsavedChanges()
        } else {
            android.util.Log.w("PreferencesViewModel", "Category not found: $categoryName")
        }
    }

    /**
     * Check if category is selected by name
     */
    fun isCategorySelected(categoryName: String): Boolean {
        val categoryId = categoryNameToId[categoryName.lowercase()]
        return categoryId != null && _uiState.value.selectedCategories.contains(categoryId)
    }

    /**
     * Toggle play style selection
     */
    fun togglePlayStyle(style: String) {
        when (style.lowercase()) {
            "casual" -> _uiState.value = _uiState.value.copy(casualSelected = !_uiState.value.casualSelected)
            "competitive" -> _uiState.value = _uiState.value.copy(competitiveSelected = !_uiState.value.competitiveSelected)
            "story-driven", "storydriven" -> _uiState.value = _uiState.value.copy(storyDrivenSelected = !_uiState.value.storyDrivenSelected)
        }
        checkForUnsavedChanges()
    }

    /**
     * Toggle monetization selection
     */
    fun toggleMonetization(monetization: String) {
        when (monetization.lowercase()) {
            "free to play", "freetoplay" -> _uiState.value = _uiState.value.copy(freeToPlaySelected = !_uiState.value.freeToPlaySelected)
            "paid" -> _uiState.value = _uiState.value.copy(paidSelected = !_uiState.value.paidSelected)
            "subscription" -> _uiState.value = _uiState.value.copy(subscriptionSelected = !_uiState.value.subscriptionSelected)
        }
        checkForUnsavedChanges()
    }

    /**
     * Save all preferences to backend
     */
    fun savePreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            
            try {
                val state = _uiState.value
                
                val platformResult = userPreferenceRepository.updatePreferences(
                    preferWindows = state.windowsSelected,
                    preferMac = state.macSelected,
                    preferLinux = state.linuxSelected,
                    preferCompetitive = state.competitiveSelected,
                    preferFreeToPlay = state.freeToPlaySelected
                )
                
                if (platformResult.isFailure) {
                    throw platformResult.exceptionOrNull() ?: Exception("Failed to update platform preferences")
                }
                
                val monetizationResult = userPreferenceRepository.updateMonetizationPreferences(
                    preferOneTimePurchase = state.paidSelected,
                    avoidSubscription = !state.subscriptionSelected
                )
                
                if (monetizationResult.isFailure) {
                    throw monetizationResult.exceptionOrNull() ?: Exception("Failed to update monetization preferences")
                }
                
                val genresList = state.selectedGenres.map { genreId ->
                    com.project.swipetoplay.data.remote.dto.GenrePreferenceItem(
                        genreId = genreId,
                        preferenceWeight = 5
                    )
                }
                
                val genresResult = userPreferenceRepository.updatePreferredGenres(genresList)
                if (genresResult.isFailure) {
                    android.util.Log.w("PreferencesViewModel", "Failed to update genres: ${genresResult.exceptionOrNull()?.message}")
                }
                
                val categoriesList = state.selectedCategories.map { categoryId ->
                    com.project.swipetoplay.data.remote.dto.CategoryPreferenceItem(
                        categoryId = categoryId,
                        preferenceWeight = 5
                    )
                }
                
                val categoriesResult = userPreferenceRepository.updatePreferredCategories(categoriesList)
                if (categoriesResult.isFailure) {
                    android.util.Log.w("PreferencesViewModel", "Failed to update categories: ${categoriesResult.exceptionOrNull()?.message}")
                }
                
                val updatedState = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    hasUnsavedChanges = false
                )
                _uiState.value = updatedState
                savedState = updatedState.copy(hasUnsavedChanges = false, saveSuccess = false)
                
                android.util.Log.d("PreferencesViewModel", "✅ Preferences saved successfully")
                
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(saveSuccess = false)
                
            } catch (e: Exception) {
                android.util.Log.e("PreferencesViewModel", "Error saving preferences", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save preferences: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Retry loading preferences
     */
    fun retry() {
        loadPreferencesData()
    }
}

