package com.project.swipetoplay.ui.features.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.GenreResponse
import com.project.swipetoplay.data.remote.dto.CategoryResponse
import com.project.swipetoplay.data.repository.UserPreferenceRepository
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
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

class PreferencesViewModel(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val gameApiService: com.project.swipetoplay.data.remote.api.GameApiService = RetrofitClient.gameApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    private val genreNameToId: MutableMap<String, Int> = mutableMapOf()
    
    private val categoryNameToId: MutableMap<String, Int> = mutableMapOf()
    
    private var savedState: PreferencesUiState? = null
    
    private var genresLoaded = false

    init {
        loadPreferencesData()
    }
    
    private fun checkForUnsavedChanges() {
        val current = _uiState.value
        val saved = savedState
        
        if (saved == null) {
            val hasAnySelections = current.windowsSelected || 
                    current.macSelected || 
                    current.linuxSelected ||
                    current.selectedGenres.isNotEmpty() ||
                    current.selectedCategories.isNotEmpty() ||
                    current.casualSelected ||
                    current.competitiveSelected ||
                    current.storyDrivenSelected ||
                    current.freeToPlaySelected ||
                    current.paidSelected ||
                    current.subscriptionSelected
            
            if (hasAnySelections) {
                _uiState.value = current.copy(hasUnsavedChanges = true)
            } else {
                savedState = current.copy(hasUnsavedChanges = false)
                _uiState.value = current.copy(hasUnsavedChanges = false)
            }
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

    private fun loadPreferencesData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
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
                
                val genresResult = genresDeferred.await()
                preferencesDeferred.await()
                
                if (genresResult.isFailure) {
                    val exception = genresResult.exceptionOrNull()
                    ErrorLogger.logError("PreferencesViewModel", "Failed to load genres: ${exception?.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorHandler.getUserFriendlyMessage(exception ?: Exception("Failed to load genres"))
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                ErrorLogger.logError("PreferencesViewModel", "Error loading preferences data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ErrorHandler.getUserFriendlyMessage(e)
                )
            }
        }
    }

    private suspend fun loadGenres(): Result<List<GenreResponse>> {
        return try {
            ErrorLogger.logDebug("PreferencesViewModel", "Starting to load genres from API")
            val response = gameApiService.getGenres()
            ErrorLogger.logDebug("PreferencesViewModel", "API response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                ErrorLogger.logDebug("PreferencesViewModel", "Response body - success: ${body?.success}, data: ${body?.data?.size} genres")
                
                if (body?.success == true && body.data != null) {
                    val genres = body.data
                    
                    if (genres.isEmpty()) {
                        ErrorLogger.logWarning("PreferencesViewModel", "Received empty genres list", null)
                        return Result.failure(Exception("No genres available"))
                    }
                    
                    genreNameToId.clear()
                    genres.forEach { genre ->
                        genreNameToId[genre.name.lowercase()] = genre.id
                    }
                    
                    _uiState.value = _uiState.value.copy(availableGenres = genres)
                    genresLoaded = true
                    ErrorLogger.logDebug("PreferencesViewModel", "Successfully loaded ${genres.size} genres: ${genres.map { it.name }}")
                    Result.success(genres)
                } else {
                    val errorMsg = body?.message ?: "Failed to fetch genres"
                    ErrorLogger.logError("PreferencesViewModel", "API returned unsuccessful response: $errorMsg", null)
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("PreferencesViewModel", "HTTP error: ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("PreferencesViewModel", "Exception while loading genres", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

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
                    ErrorLogger.logDebug("PreferencesViewModel", "Loaded ${categories.size} categories")
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

    private suspend fun loadUserPreferences() {
        try {
            val result = userPreferenceRepository.getPreferences()
            result.onSuccess { preferences ->
                ErrorLogger.logDebug("PreferencesViewModel", "Loaded user preferences")
                
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
                ErrorLogger.logWarning("PreferencesViewModel", "Failed to load preferences: ${error.message}", error)

                if (error.message?.contains("500") == true || error.message?.contains("Internal Server Error") == true) {
                    ErrorLogger.logError("PreferencesViewModel", "Server error loading preferences - user may not have preferences yet", error)
                }
                
                val currentState = _uiState.value
                savedState = currentState.copy(hasUnsavedChanges = false)
            }
        } catch (e: Exception) {
            ErrorLogger.logError("PreferencesViewModel", "Error loading user preferences", e)
            val currentState = _uiState.value
            savedState = currentState.copy(hasUnsavedChanges = false)
        }
    }

    fun togglePlatform(platform: String) {
        when (platform.lowercase()) {
            "windows" -> _uiState.value = _uiState.value.copy(windowsSelected = !_uiState.value.windowsSelected)
            "mac" -> _uiState.value = _uiState.value.copy(macSelected = !_uiState.value.macSelected)
            "linux" -> _uiState.value = _uiState.value.copy(linuxSelected = !_uiState.value.linuxSelected)
        }
        checkForUnsavedChanges()
    }

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
            ErrorLogger.logWarning("PreferencesViewModel", "Genre not found: $genreName", null)
        }
    }

    fun isGenreSelected(genreName: String): Boolean {
        val genreId = genreNameToId[genreName.lowercase()]
        return genreId != null && _uiState.value.selectedGenres.contains(genreId)
    }

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
            ErrorLogger.logWarning("PreferencesViewModel", "Category not found: $categoryName", null)
        }
    }

    fun isCategorySelected(categoryName: String): Boolean {
        val categoryId = categoryNameToId[categoryName.lowercase()]
        return categoryId != null && _uiState.value.selectedCategories.contains(categoryId)
    }

    fun togglePlayStyle(style: String) {
        when (style.lowercase()) {
            "casual" -> _uiState.value = _uiState.value.copy(casualSelected = !_uiState.value.casualSelected)
            "competitive" -> _uiState.value = _uiState.value.copy(competitiveSelected = !_uiState.value.competitiveSelected)
            "story-driven", "storydriven" -> _uiState.value = _uiState.value.copy(storyDrivenSelected = !_uiState.value.storyDrivenSelected)
        }
        checkForUnsavedChanges()
    }

    fun toggleMonetization(monetization: String) {
        when (monetization.lowercase()) {
            "free to play", "freetoplay" -> _uiState.value = _uiState.value.copy(freeToPlaySelected = !_uiState.value.freeToPlaySelected)
            "paid" -> _uiState.value = _uiState.value.copy(paidSelected = !_uiState.value.paidSelected)
            "subscription" -> _uiState.value = _uiState.value.copy(subscriptionSelected = !_uiState.value.subscriptionSelected)
        }
        checkForUnsavedChanges()
    }

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
                    ErrorLogger.logWarning("PreferencesViewModel", "Failed to update genres: ${genresResult.exceptionOrNull()?.message}", genresResult.exceptionOrNull())
                }
                
                val categoriesList = state.selectedCategories.map { categoryId ->
                    com.project.swipetoplay.data.remote.dto.CategoryPreferenceItem(
                        categoryId = categoryId,
                        preferenceWeight = 5
                    )
                }
                
                val categoriesResult = userPreferenceRepository.updatePreferredCategories(categoriesList)
                if (categoriesResult.isFailure) {
                    ErrorLogger.logWarning("PreferencesViewModel", "Failed to update categories: ${categoriesResult.exceptionOrNull()?.message}", categoriesResult.exceptionOrNull())
                }
                
                val updatedState = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    hasUnsavedChanges = false
                )
                _uiState.value = updatedState
                savedState = updatedState.copy(hasUnsavedChanges = false, saveSuccess = false)
                
                ErrorLogger.logDebug("PreferencesViewModel", "Preferences saved successfully")
                
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(saveSuccess = false)
                
            } catch (e: Exception) {
                ErrorLogger.logError("PreferencesViewModel", "Error saving preferences", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = ErrorHandler.getUserFriendlyMessage(e)
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retry() {
        loadPreferencesData()
    }
}

