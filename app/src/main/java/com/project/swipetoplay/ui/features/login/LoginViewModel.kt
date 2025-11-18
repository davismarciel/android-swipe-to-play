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

data class LoginUiState(
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val isCheckingApi: Boolean = false,
    val user: GoogleUser? = null,
    val errorMessage: String? = null,
    val isApiOnline: Boolean? = null
)

sealed class LoginEvent {
    data object SignInRequested : LoginEvent()
    data class SignInSuccess(val user: GoogleUser) : LoginEvent()
    data class SignInError(val message: String) : LoginEvent()
    data object SignInCancelled : LoginEvent()
}

class LoginViewModel(
    private val authManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkApiHealth()
        checkExistingAuth()
        
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(3000)
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

    private fun checkApiHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingApi = true) }
            try {
                val authApiService = com.project.swipetoplay.data.remote.api.RetrofitClient.authApiService
                val response = authApiService.health()
                
                val isOnline = response.isSuccessful && response.body()?.success == true
                _uiState.update { 
                    it.copy(
                        isCheckingApi = false,
                        isApiOnline = isOnline
                    )
                }
                
                if (!isOnline) {
                    ErrorLogger.logWarning("LoginViewModel", "API health check failed: ${response.code()}", null)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "A API está offline ou indisponível. Por favor, verifique sua conexão e tente novamente."
                        )
                    }
                } else {
                    ErrorLogger.logDebug("LoginViewModel", "API health check successful")
                }
            } catch (e: Exception) {
                ErrorLogger.logError("LoginViewModel", "Error checking API health: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isCheckingApi = false,
                        isApiOnline = false,
                        errorMessage = "Não foi possível conectar à API. Verifique sua conexão com a internet e tente novamente."
                    )
                }
            }
        }
    }

    private fun checkExistingAuth() {
        viewModelScope.launch {
            while (_uiState.value.isCheckingApi) {
                kotlinx.coroutines.delay(100)
            }
            
            if (_uiState.value.isApiOnline != true) {
                ErrorLogger.logWarning("LoginViewModel", "Skipping auth check - API is offline")
                return@launch
            }
            
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
                _uiState.update { 
                    it.copy(
                        isApiOnline = false,
                        errorMessage = "Erro ao verificar credenciais. A API pode estar offline."
                    )
                }
            }
        }
    }

    fun onSignInClick() {
        ErrorLogger.logDebug("LoginViewModel", "User clicked Sign-In button")
        
        if (_uiState.value.isApiOnline == false) {
            ErrorLogger.logWarning("LoginViewModel", "Sign-in blocked - API is offline")
            _uiState.update { 
                it.copy(
                    errorMessage = "A API está offline ou indisponível. Por favor, verifique sua conexão e tente novamente."
                )
            }
            checkApiHealth()
            return
        }
        
        if (_uiState.value.isApiOnline == null) {
            ErrorLogger.logDebug("LoginViewModel", "API status unknown, checking health first")
            checkApiHealth()
            viewModelScope.launch {
                while (_uiState.value.isCheckingApi) {
                    kotlinx.coroutines.delay(100)
                }
                if (_uiState.value.isApiOnline == true) {
                    onSignInClick()
                }
            }
            return
        }
        
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
                val errorMessage = ErrorHandler.getUserFriendlyMessage(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isValidating = false,
                        user = null,
                        errorMessage = errorMessage,
                        isApiOnline = false
                    )
                }
                checkApiHealth()
            }
        }
    }

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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retryApiCheck() {
        viewModelScope.launch {
            checkApiHealth()
        }
    }

    fun signOut() {
        ErrorLogger.logDebug("LoginViewModel", "Signing out user")
        viewModelScope.launch {
            authManager.signOut()
            _uiState.update { LoginUiState() }
            ErrorLogger.logDebug("LoginViewModel", "User signed out, state reset")
        }
    }
}

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

