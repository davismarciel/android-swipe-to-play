package com.project.swipetoplay.data.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.project.swipetoplay.BuildConfig
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.LoginRequest
import com.project.swipetoplay.data.auth.TokenManager
import com.project.swipetoplay.domain.model.AuthResult
import com.project.swipetoplay.domain.model.GoogleUser
import com.project.swipetoplay.data.error.ErrorLogger
import com.project.swipetoplay.data.error.ErrorHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manager class for handling Google Sign-In authentication using Credential Manager.
 *
 * @property context Application context
 */
class GoogleAuthManager(private val context: Context) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)
    
    private val tokenManager: TokenManager by lazy {
        val manager = RetrofitClient.getTokenManager()
        if (manager == null) {
            ErrorLogger.logWarning("GoogleAuthManager", "RetrofitClient not initialized yet, creating fallback TokenManager")
            TokenManager(context)
        } else {
            ErrorLogger.logDebug("GoogleAuthManager", "Using shared TokenManager from RetrofitClient")
            manager
        }
    }

    companion object {
        private var lastIdToken: String? = null
        private var loginCount = 0
    }

    /**
     * Initiates the Google Sign-In flow using Credential Manager.
     * This method handles the Google account selection and returns immediately.
     * Backend validation happens separately.
     *
     * @return AuthResult indicating success, error, or cancellation
     */
    suspend fun signInWithGoogle(): AuthResult {
        return try {
            ErrorLogger.logDebug("GoogleAuth", "Starting Google Sign-In flow")
            ErrorLogger.logDebug("GoogleAuth", "Web Client ID configured: ${BuildConfig.WEB_CLIENT_ID.take(20)}...")

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                ErrorLogger.logDebug("GoogleAuth", "Attempting to get existing credentials")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: NoCredentialException) {
                ErrorLogger.logDebug("GoogleAuth", "No existing credentials found, showing account picker")
                signInWithGoogleNewUser()
            }
        } catch (e: GetCredentialCancellationException) {
            ErrorLogger.logWarning("GoogleAuth", "Sign-in was cancelled by user", e)
            if (e.errorMessage != null) {
                ErrorLogger.logWarning("GoogleAuth", "Cancellation error message: ${e.errorMessage}")
            }
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            ErrorLogger.logError("GoogleAuth", "Authentication failed: ${e.message}", e)
            if (e.errorMessage != null) {
                ErrorLogger.logError("GoogleAuth", "Detailed error: ${e.errorMessage}")
            }
            AuthResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        } catch (e: CancellationException) {
            ErrorLogger.logWarning("GoogleAuth", "Sign-in was cancelled", e)
            AuthResult.Cancelled
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "Unknown error occurred: ${e.message}", e)
            AuthResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * Handles sign-in for new users or when no existing credentials are found.
     *
     * @return AuthResult indicating success, error, or cancellation
     */
    private suspend fun signInWithGoogleNewUser(): AuthResult {
        return try {
            ErrorLogger.logDebug("GoogleAuth", "Starting new user sign-in flow")

            val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            handleSignIn(result)
        } catch (e: GetCredentialCancellationException) {
            ErrorLogger.logWarning("GoogleAuth", "New user sign-in was cancelled", e)
            if (e.errorMessage != null) {
                ErrorLogger.logWarning("GoogleAuth", "Cancellation error message: ${e.errorMessage}")
            }
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            ErrorLogger.logError("GoogleAuth", "New user authentication failed: ${e.message}", e)
            if (e.errorMessage != null) {
                ErrorLogger.logError("GoogleAuth", "Detailed error: ${e.errorMessage}")
            }
            AuthResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        } catch (e: CancellationException) {
            ErrorLogger.logWarning("GoogleAuth", "New user sign-in was cancelled", e)
            AuthResult.Cancelled
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "Unknown error in new user flow: ${e.message}", e)
            AuthResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * Processes the credential response and extracts user information.
     *
     * @param result The credential response from Credential Manager
     * @return AuthResult with user data or error
     */
    private fun handleSignIn(result: GetCredentialResponse): AuthResult {
        return try {
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken
                loginCount++

                val isNewToken = compareTokens(idToken)
                val tokenInfo = getTokenExpirationInfo(idToken)

                ErrorLogger.logDebug("GoogleAuth", "Google Sign-In Success (Login #$loginCount)")
                ErrorLogger.logDebug("GoogleAuth", "NEW TOKEN GENERATED: ${if (isNewToken) "YES" else "NO (Same as previous)"}")
                ErrorLogger.logDebug("GoogleAuth", "Token Info: $tokenInfo")
                ErrorLogger.logDebug("GoogleAuth", "User ID: ${googleIdTokenCredential.id}")
                ErrorLogger.logDebug("GoogleAuth", "User Email: ${googleIdTokenCredential.id}")
                ErrorLogger.logDebug("GoogleAuth", "Display Name: ${googleIdTokenCredential.displayName}")
                ErrorLogger.logDebug("GoogleAuth", "Profile Picture: ${googleIdTokenCredential.profilePictureUri}")
                ErrorLogger.logDebug("GoogleAuth", "Token Preview (first 50): ${idToken.take(50)}...")
                ErrorLogger.logDebug("GoogleAuth", "Token Preview (last 20): ...${idToken.takeLast(20)}")
                ErrorLogger.logDebug("GoogleAuth", "Token Length: ${idToken.length} characters")

                lastIdToken = idToken

                val user = GoogleUser(
                    id = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )

                AuthResult.Success(user)
            } else {
                ErrorLogger.logError("GoogleAuth", "Invalid credential type: ${credential::class.simpleName}")
                AuthResult.Error("Invalid credential type")
            }
        } catch (e: GoogleIdTokenParsingException) {
            ErrorLogger.logError("GoogleAuth", "Failed to parse Google ID Token", e)
            AuthResult.Error("Failed to process Google token")
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "Failed to process credentials", e)
            AuthResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * Validates the current user with the backend server.
     * This should be called after successful Google authentication.
     *
     * @return BackendValidationResult indicating success or failure
     */
    suspend fun validateWithBackend(): BackendValidationResult {
        return lastIdToken?.let { token ->
            sendTokenToBackendSyncSuspend(token)
        } ?: BackendValidationResult.Error("No token available for validation")
    }

    /**
     * Signs out the user by clearing the credential state and JWT token.
     */
    suspend fun signOut() {
        try {
            ErrorLogger.logDebug("GoogleAuth", "Signing out user")
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            tokenManager.clearToken()
            ErrorLogger.logDebug("GoogleAuth", "Sign-out successful")
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "Sign-out error: ${e.message}", e)
        }
    }

    /**
     * Decodes JWT token and extracts expiration time
     */
    private fun getTokenExpirationInfo(idToken: String): String {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) return "Invalid token format"

            val payload = parts[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val json = JSONObject(String(decoded, Charsets.UTF_8))

            val expSeconds = json.optLong("exp", 0L)
            val iatSeconds = json.optLong("iat", 0L)

            if (expSeconds == 0L) return "No expiration found"

            val expDate = Date(expSeconds * 1000L)
            val iatDate = Date(iatSeconds * 1000L)
            val now = System.currentTimeMillis()
            val remainingSeconds = (expSeconds * 1000L - now) / 1000L

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            buildString {
                append("Issued at: ${dateFormat.format(iatDate)}")
                append(", Expires at: ${dateFormat.format(expDate)}")
                append(", Remaining: ${remainingSeconds}s (${remainingSeconds / 60} min)")
            }
        } catch (e: Exception) {
            "Error decoding token: ${e.message}"
        }
    }

    /**
     * Compares current token with last token to check if it's new
     */
    private fun compareTokens(currentToken: String): Boolean {
        return currentToken != lastIdToken
    }

    /**
     * Result of backend validation
     */
    sealed class BackendValidationResult {
        data object Success : BackendValidationResult()
        data class Error(val message: String) : BackendValidationResult()
    }

    /**
     * Sends the ID token to the backend API for validation (synchronous)
     */
    private fun sendTokenToBackendSync(idToken: String): BackendValidationResult {
        return runBlocking {
            sendTokenToBackendSyncSuspend(idToken)
        }
    }

    /**
     * Sends the ID token to the backend API for validation (synchronous suspend)
     */
    private suspend fun sendTokenToBackendSyncSuspend(idToken: String): BackendValidationResult {
        return try {
            ErrorLogger.logDebug("GoogleAuth", "SENDING TO BACKEND")
            ErrorLogger.logDebug("GoogleAuth", "Base URL: ${BuildConfig.API_BASE_URL}")
            ErrorLogger.logDebug("GoogleAuth", "Full Endpoint: ${BuildConfig.API_BASE_URL}api/v1/auth/login")

            val request = LoginRequest(idToken = idToken)
            val response = RetrofitClient.authApiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                ErrorLogger.logDebug("GoogleAuth", "BACKEND SUCCESS")
                ErrorLogger.logDebug("GoogleAuth", "Response code: ${response.code()}")
                ErrorLogger.logDebug("GoogleAuth", "Access Token: ${loginResponse?.data?.accessToken?.take(50)}...")
                ErrorLogger.logDebug("GoogleAuth", "Token Type: ${loginResponse?.data?.tokenType}")
                ErrorLogger.logDebug("GoogleAuth", "Backend User: ${loginResponse?.data?.user?.name} (${loginResponse?.data?.user?.email})")
                ErrorLogger.logDebug("GoogleAuth", "Message: ${loginResponse?.message}")
                
                loginResponse?.data?.let { data ->
                    data.accessToken?.let { token ->
                        val tokenType = data.tokenType ?: "Bearer"
                        val normalizedTokenType = if (tokenType.equals("bearer", ignoreCase = true)) {
                            "Bearer"
                        } else {
                            tokenType
                        }
                        
                        tokenManager.saveToken(
                            accessToken = token,
                            tokenType = normalizedTokenType,
                            expiresIn = data.expiresIn
                        )
                        ErrorLogger.logDebug("GoogleAuth", "JWT token saved successfully")
                        ErrorLogger.logDebug("GoogleAuth", "Token type: $normalizedTokenType")
                        ErrorLogger.logDebug("GoogleAuth", "Token length: ${token.length}")
                        
                        val savedToken = tokenManager.getAccessToken()
                        val savedType = tokenManager.getTokenType()
                        if (savedToken != null) {
                            ErrorLogger.logDebug("GoogleAuth", "Token verification: Saved successfully (length: ${savedToken.length}, type: $savedType)")
                            ErrorLogger.logDebug("GoogleAuth", "Authorization header: ${tokenManager.getAuthorizationHeader()?.take(30)}...")
                        } else {
                            ErrorLogger.logError("GoogleAuth", "Token verification: Failed to save token")
                        }
                    }
                }
                
                BackendValidationResult.Success
            } else {
                val errorBody = response.errorBody()?.string()
                ErrorLogger.logError("GoogleAuth", "BACKEND FAILED")
                ErrorLogger.logError("GoogleAuth", "Response code: ${response.code()}")
                ErrorLogger.logError("GoogleAuth", "Error body: $errorBody")
                BackendValidationResult.Error(ErrorHandler.getUserFriendlyMessage(response.code(), errorBody))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "CONNECTION ERROR", e)
            ErrorLogger.logError("GoogleAuth", "Exception type: ${e::class.simpleName}")
            ErrorLogger.logError("GoogleAuth", "Error message: ${e.message}")
            ErrorLogger.logError("GoogleAuth", "Troubleshooting: Backend running? Same WiFi network? Firewall blocking port? API_BASE_URL correct? Current: ${BuildConfig.API_BASE_URL}")
            BackendValidationResult.Error(ErrorHandler.getUserFriendlyMessage(e))
        }
    }

    /**
     * Sends the ID token to the backend API for validation (legacy async method)
     */
    private suspend fun sendTokenToBackend(idToken: String) {
        try {
            ErrorLogger.logDebug("GoogleAuth", "SENDING TO BACKEND")
            ErrorLogger.logDebug("GoogleAuth", "Base URL: ${BuildConfig.API_BASE_URL}")
            ErrorLogger.logDebug("GoogleAuth", "Full Endpoint: ${BuildConfig.API_BASE_URL}api/v1/auth/login")

            val request = LoginRequest(idToken = idToken)
            val response = RetrofitClient.authApiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                ErrorLogger.logDebug("GoogleAuth", "BACKEND SUCCESS")
                ErrorLogger.logDebug("GoogleAuth", "Response code: ${response.code()}")
                ErrorLogger.logDebug("GoogleAuth", "Access Token: ${loginResponse?.data?.accessToken?.take(50)}...")
                ErrorLogger.logDebug("GoogleAuth", "Token Type: ${loginResponse?.data?.tokenType}")
                ErrorLogger.logDebug("GoogleAuth", "Backend User: ${loginResponse?.data?.user?.name} (${loginResponse?.data?.user?.email})")
                ErrorLogger.logDebug("GoogleAuth", "Message: ${loginResponse?.message}")
            } else {
                val errorBody = response.errorBody()?.string()
                ErrorLogger.logError("GoogleAuth", "BACKEND FAILED")
                ErrorLogger.logError("GoogleAuth", "Response code: ${response.code()}")
                ErrorLogger.logError("GoogleAuth", "Error body: $errorBody")
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GoogleAuth", "CONNECTION ERROR", e)
            ErrorLogger.logError("GoogleAuth", "Exception type: ${e::class.simpleName}")
            ErrorLogger.logError("GoogleAuth", "Error message: ${e.message}")
            ErrorLogger.logError("GoogleAuth", "Troubleshooting: Backend running? Same WiFi network? Firewall blocking port? API_BASE_URL correct? Current: ${BuildConfig.API_BASE_URL}")
        }
    }
}

