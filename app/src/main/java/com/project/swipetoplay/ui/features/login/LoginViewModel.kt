package com.project.swipetoplay.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.auth.GoogleAuthManager
import com.project.swipetoplay.domain.model.AuthResult
import com.project.swipetoplay.domain.model.GoogleUser
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class representing the UI state for the Login screen.
 *
 * @property isLoading Whether Google authentication is in progress
 * @property isValidating Whether backend validation is in progress
 * @property user The authenticated user, null if not authenticated
 * @property errorMessage Error message to display, null if no error
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val user: GoogleUser? = null,
    val errorMessage: String? = null
)

/**
 * Sealed class representing events that can occur during login.
 */
sealed class LoginEvent {
    /**
     * User requested to sign in.
     */
    data object SignInRequested : LoginEvent()

    /**
     * Sign-in was successful.
     *
     * @property user The authenticated user
     */
    data class SignInSuccess(val user: GoogleUser) : LoginEvent()

    /**
     * Sign-in failed with an error.
     *
     * @property message Error message
     */
    data class SignInError(val message: String) : LoginEvent()

    /**
     * Sign-in was cancelled by the user.
     */
    data object SignInCancelled : LoginEvent()
}

/**
 * ViewModel for managing Login screen state and authentication logic.
 *
 * @property authManager Manager for handling Google authentication
 */
class LoginViewModel(
    private val authManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingAuth()
        
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(3000) // Check every 3 seconds (reduzido para economizar recursos)
                val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
                val currentUser = _uiState.value.user
                
                if (currentUser != null) {
                    if (tokenManager?.isAuthenticated() != true) {
                        ErrorLogger.logWarning("LoginViewModel", "Token was cleared while user was authenticated, logging out", null)
                        _uiState.update { 
                            it.copy(
                                user = null,
                                errorMessage = "Sua sessão expirou. Por favor, faça login novamente."
                            )
                        }
                        break
                    }
                } else {
                    kotlinx.coroutines.delay(5000)
                }
            }
        }
    }

    /**
     * Checks if there's an existing valid token and restores user session
     */
    private fun checkExistingAuth() {
        viewModelScope.launch {
            try {
                val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
                if (tokenManager?.isAuthenticated() == true) {
                    ErrorLogger.logDebug("LoginViewModel", "Existing token found, verifying with backend")
                    
                    val authApiService = com.project.swipetoplay.data.remote.api.RetrofitClient.authApiService
                    val response = authApiService.getCurrentUser()
                    
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            val userData = body.data["user"] as? Map<*, *>
                            if (userData != null) {
                                val user = GoogleUser(
                                    id = (userData["email"] as? String) ?: "",
                                    email = (userData["email"] as? String) ?: "",
                                    displayName = (userData["name"] as? String) ?: "",
                                    profilePictureUrl = (userData["avatar"] as? String)
                                )
                                
                                ErrorLogger.logDebug("LoginViewModel", "Token verified, user restored: ${user.displayName}")
                                _uiState.update { 
                                    it.copy(user = user)
                                }
                                return@launch
                            }
                        }
                    } else {
                        ErrorLogger.logWarning("LoginViewModel", "Token verification failed: ${response.code()}, clearing token")
                        tokenManager.clearToken()
                    }
                } else {
                    ErrorLogger.logDebug("LoginViewModel", "No existing token found")
                }
            } catch (e: Exception) {
                ErrorLogger.logError("LoginViewModel", "Error checking existing auth: ${e.message}", e)
                com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()?.clearToken()
            }
        }
    }

    /**
     * Initiates the Google Sign-In process.
     */
    fun onSignInClick() {
        ErrorLogger.logDebug("LoginViewModel", "User clicked Sign-In button")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = authManager.signInWithGoogle()
                
                when (result) {
                    is AuthResult.Success -> {
                        ErrorLogger.logDebug("LoginViewModel", "Google authentication successful for user: ${result.user.displayName}")
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isValidating = true,
                                user = result.user
                            ) 
                        }
                        
                        val backendResult = authManager.validateWithBackend()
                        
                        when (backendResult) {
                            is GoogleAuthManager.BackendValidationResult.Success -> {
                                ErrorLogger.logDebug("LoginViewModel", "Backend validation successful")
                                _uiState.update {
                                    it.copy(
                                        isValidating = false,
                                        user = result.user,
                                        errorMessage = null
                                    )
                                }
                            }
                            is GoogleAuthManager.BackendValidationResult.Error -> {
                                ErrorLogger.logError("LoginViewModel", "Backend validation failed: ${backendResult.message}", null)
                                _uiState.update {
                                    it.copy(
                                        isValidating = false,
                                        user = null,
                                        errorMessage = backendResult.message
                                    )
                                }
                            }
                        }
                    }
                    is AuthResult.Error -> {
                        ErrorLogger.logError("LoginViewModel", "Google authentication error: ${result.message}", null)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = result.message
                            )
                        }
                    }
                    is AuthResult.Cancelled -> {
                        ErrorLogger.logWarning("LoginViewModel", "Google authentication was cancelled", null)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                ErrorLogger.logError("LoginViewModel", "Sign-in error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isValidating = false,
                        user = null,
                        errorMessage = ErrorHandler.getUserFriendlyMessage(e)
                    )
                }
            }
        }
    }

    /**
     * Processes the authentication result and updates UI state accordingly.
     *
     * @param result The result of the authentication attempt
     */
    fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                ErrorLogger.logDebug("LoginViewModel", "Authentication successful for user: ${result.user.displayName}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = result.user,
                        errorMessage = null
                    )
                }
            }
            is AuthResult.Error -> {
                ErrorLogger.logError("LoginViewModel", "Authentication error: ${result.message}", null)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        errorMessage = result.message
                    )
                }
            }
            is AuthResult.Cancelled -> {
                ErrorLogger.logWarning("LoginViewModel", "Authentication was cancelled", null)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        errorMessage = null
                    )
                }
            }
        }
    }

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        ErrorLogger.logDebug("LoginViewModel", "Signing out user")
        viewModelScope.launch {
            authManager.signOut()
            _uiState.update { LoginUiState() }
            ErrorLogger.logDebug("LoginViewModel", "User signed out, state reset")
        }
    }
}

/**
 * Factory for creating LoginViewModel instances with dependencies.
 */
class LoginViewModelFactory(
    private val authManager: GoogleAuthManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

