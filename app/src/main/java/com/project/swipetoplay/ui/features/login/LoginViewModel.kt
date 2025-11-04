package com.project.swipetoplay.ui.features.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.swipetoplay.data.auth.GoogleAuthManager
import com.project.swipetoplay.domain.model.AuthResult
import com.project.swipetoplay.domain.model.GoogleUser
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

    /**
     * Initiates the Google Sign-In process.
     */
    fun onSignInClick() {
        Log.d("LoginViewModel", "User clicked Sign-In button")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = authManager.signInWithGoogle()
                
                when (result) {
                    is AuthResult.Success -> {
                        Log.d("LoginViewModel", "Google authentication successful for user: ${result.user.displayName}")
                        
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
                                Log.d("LoginViewModel", "Backend validation successful")
                                _uiState.update {
                                    it.copy(
                                        isValidating = false,
                                        user = result.user,
                                        errorMessage = null
                                    )
                                }
                            }
                            is GoogleAuthManager.BackendValidationResult.Error -> {
                                Log.e("LoginViewModel", "Backend validation failed: ${backendResult.message}")
                                _uiState.update {
                                    it.copy(
                                        isValidating = false,
                                        user = null,
                                        errorMessage = "Validation failed: ${backendResult.message}"
                                    )
                                }
                            }
                        }
                    }
                    is AuthResult.Error -> {
                        Log.e("LoginViewModel", "Google authentication error: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = result.message
                            )
                        }
                    }
                    is AuthResult.Cancelled -> {
                        Log.w("LoginViewModel", "Google authentication was cancelled")
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
                Log.e("LoginViewModel", "Sign-in error: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isValidating = false,
                        user = null,
                        errorMessage = "Authentication error: ${e.message}"
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
                Log.d("LoginViewModel", "Authentication successful for user: ${result.user.displayName}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = result.user,
                        errorMessage = null
                    )
                }
            }
            is AuthResult.Error -> {
                Log.e("LoginViewModel", "Authentication error: ${result.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        errorMessage = result.message
                    )
                }
            }
            is AuthResult.Cancelled -> {
                Log.w("LoginViewModel", "Authentication was cancelled")
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
        Log.d("LoginViewModel", "Signing out user")
        viewModelScope.launch {
            authManager.signOut()
            _uiState.update { LoginUiState() }
            Log.d("LoginViewModel", "User signed out, state reset")
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

