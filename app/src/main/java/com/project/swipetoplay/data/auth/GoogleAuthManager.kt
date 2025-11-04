package com.project.swipetoplay.data.auth

import android.content.Context
import android.util.Base64
import android.util.Log
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
            android.util.Log.e("GoogleAuthManager", "❌ WARNING: RetrofitClient not initialized yet!")
            android.util.Log.e("GoogleAuthManager", "❌ Creating fallback TokenManager - this may cause token sync issues!")
            TokenManager(context)
        } else {
            android.util.Log.d("GoogleAuthManager", "✅ Using shared TokenManager from RetrofitClient")
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
                Log.d("GoogleAuth", "Starting Google Sign-In flow...")
            Log.d("GoogleAuth", "Web Client ID configured: ${BuildConfig.WEB_CLIENT_ID.take(20)}...")

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                Log.d("GoogleAuth", "Attempting to get existing credentials...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: NoCredentialException) {
                Log.d("GoogleAuth", "No existing credentials found, showing account picker...")
                signInWithGoogleNewUser()
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("GoogleAuth", "Sign-in was cancelled by user")
            Log.w("GoogleAuth", "Cancellation details: ${e::class.simpleName} - ${e.message}")
            Log.w("GoogleAuth", "Error type: ${e.type}")
            if (e.errorMessage != null) {
                Log.w("GoogleAuth", "Error message: ${e.errorMessage}")
            }
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "Authentication failed: ${e.message}", e)
            Log.e("GoogleAuth", "Exception type: ${e::class.simpleName}")
            Log.e("GoogleAuth", "Error type: ${e.type}")
            if (e.errorMessage != null) {
                Log.e("GoogleAuth", "Detailed error: ${e.errorMessage}")
            }
            AuthResult.Error(e.message ?: "Authentication failed")
        } catch (e: CancellationException) {
            Log.w("GoogleAuth", "Sign-in was cancelled")
            AuthResult.Cancelled
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Unknown error occurred: ${e.message}", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Handles sign-in for new users or when no existing credentials are found.
     *
     * @return AuthResult indicating success, error, or cancellation
     */
    private suspend fun signInWithGoogleNewUser(): AuthResult {
        return try {
            Log.d("GoogleAuth", "Starting new user sign-in flow...")

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
            Log.w("GoogleAuth", "New user sign-in was cancelled")
            Log.w("GoogleAuth", "Cancellation details: ${e::class.simpleName} - ${e.message}")
            Log.w("GoogleAuth", "Error type: ${e.type}")
            if (e.errorMessage != null) {
                Log.w("GoogleAuth", "Error message: ${e.errorMessage}")
            }
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "New user authentication failed: ${e.message}", e)
            Log.e("GoogleAuth", "Exception type: ${e::class.simpleName}")
            Log.e("GoogleAuth", "Error type: ${e.type}")
            if (e.errorMessage != null) {
                Log.e("GoogleAuth", "Detailed error: ${e.errorMessage}")
            }
            AuthResult.Error(e.message ?: "Authentication failed")
        } catch (e: CancellationException) {
            Log.w("GoogleAuth", "New user sign-in was cancelled")
            AuthResult.Cancelled
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Unknown error in new user flow: ${e.message}", e)
            AuthResult.Error(e.message ?: "Unknown error occurred")
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

                Log.d("GoogleAuth", "==========================================================")
                Log.d("GoogleAuth", "=== Google Sign-In Success (Login #$loginCount) ===")
                Log.d("GoogleAuth", "==========================================================")
                Log.d("GoogleAuth", "✅ NEW TOKEN GENERATED: ${if (isNewToken) "YES" else "NO (Same as previous)"}")
                Log.d("GoogleAuth", "📅 Token Info: $tokenInfo")
                Log.d("GoogleAuth", "")
                Log.d("GoogleAuth", "👤 User ID: ${googleIdTokenCredential.id}")
                Log.d("GoogleAuth", "📧 User Email: ${googleIdTokenCredential.id}")
                Log.d("GoogleAuth", "📝 Display Name: ${googleIdTokenCredential.displayName}")
                Log.d("GoogleAuth", "🖼️  Profile Picture: ${googleIdTokenCredential.profilePictureUri}")
                Log.d("GoogleAuth", "")
                Log.d("GoogleAuth", "📋 ========== COPY ID TOKEN BELOW ==========")
                Log.d("GoogleAuth", idToken)
                Log.d("GoogleAuth", "📋 ========== COPY ID TOKEN ABOVE ==========")
                Log.d("GoogleAuth", "")
                Log.d("GoogleAuth", "🔑 Token Preview (first 50): ${idToken.take(50)}...")
                Log.d("GoogleAuth", "🔑 Token Preview (last 20): ...${idToken.takeLast(20)}")
                Log.d("GoogleAuth", "🔑 Token Length: ${idToken.length} characters")
                Log.d("GoogleAuth", "==========================================================")

                lastIdToken = idToken

                val user = GoogleUser(
                    id = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )

                AuthResult.Success(user)
            } else {
                Log.e("GoogleAuth", "Invalid credential type: ${credential::class.simpleName}")
                AuthResult.Error("Invalid credential type")
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleAuth", "Failed to parse Google ID Token", e)
            AuthResult.Error("Failed to parse Google ID Token: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Failed to process credentials", e)
            AuthResult.Error(e.message ?: "Failed to process credentials")
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
            Log.d("GoogleAuth", "Signing out user...")
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            tokenManager.clearToken()
            Log.d("GoogleAuth", "Sign-out successful")
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Sign-out error: ${e.message}", e)
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
            Log.d("GoogleAuth", "")
            Log.d("GoogleAuth", "🌐 ========== SENDING TO BACKEND ==========")
            Log.d("GoogleAuth", "📍 Base URL: ${BuildConfig.API_BASE_URL}")
            Log.d("GoogleAuth", "📍 Full Endpoint: ${BuildConfig.API_BASE_URL}api/v1/auth/login")
            Log.d("GoogleAuth", "")
            Log.d("GoogleAuth", "📤 Request Body:")
            Log.d("GoogleAuth", "   {")
            Log.d("GoogleAuth", "     \"id_token\": \"$idToken\"")
            Log.d("GoogleAuth", "   }")
            Log.d("GoogleAuth", "")

            val request = LoginRequest(idToken = idToken)
            val response = RetrofitClient.authApiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                Log.d("GoogleAuth", "")
                Log.d("GoogleAuth", "✅ ========== BACKEND SUCCESS ==========")
                Log.d("GoogleAuth", "📦 Response code: ${response.code()}")
                Log.d("GoogleAuth", "🔑 Access Token: ${loginResponse?.data?.accessToken?.take(50)}...")
                Log.d("GoogleAuth", "🏷️  Token Type: ${loginResponse?.data?.tokenType}")
                Log.d("GoogleAuth", "👤 Backend User: ${loginResponse?.data?.user?.name} (${loginResponse?.data?.user?.email})")
                Log.d("GoogleAuth", "💬 Message: ${loginResponse?.message}")
                Log.d("GoogleAuth", "==========================================================")
                
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
                        Log.d("GoogleAuth", "💾 JWT token saved successfully")
                        Log.d("GoogleAuth", "📝 Token type: $normalizedTokenType")
                        Log.d("GoogleAuth", "📝 Token length: ${token.length}")
                        
                        val savedToken = tokenManager.getAccessToken()
                        val savedType = tokenManager.getTokenType()
                        if (savedToken != null) {
                            Log.d("GoogleAuth", "✅ Token verification: Saved successfully (length: ${savedToken.length}, type: $savedType)")
                            Log.d("GoogleAuth", "📋 Authorization header: ${tokenManager.getAuthorizationHeader()?.take(30)}...")
                        } else {
                            Log.e("GoogleAuth", "❌ Token verification: Failed to save token!")
                        }
                    }
                }
                
                BackendValidationResult.Success
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GoogleAuth", "")
                Log.e("GoogleAuth", "❌ ========== BACKEND FAILED ==========")
                Log.e("GoogleAuth", "📦 Response code: ${response.code()}")
                Log.e("GoogleAuth", "📝 Error body: $errorBody")
                Log.e("GoogleAuth", "========================================")
                BackendValidationResult.Error("Backend validation failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("GoogleAuth", "")
            Log.e("GoogleAuth", "❌ ========== CONNECTION ERROR ==========")
            Log.e("GoogleAuth", "🔍 Exception type: ${e::class.simpleName}")
            Log.e("GoogleAuth", "💬 Error message: ${e.message}")
            Log.e("GoogleAuth", "")
            Log.e("GoogleAuth", "🔧 Troubleshooting:")
            Log.e("GoogleAuth", "   1. Backend running? python manage.py runserver 0.0.0.0:8000")
            Log.e("GoogleAuth", "   2. Same WiFi network?")
            Log.e("GoogleAuth", "   3. Firewall blocking port 8000?")
            Log.e("GoogleAuth", "   4. API_BASE_URL correct in local.properties?")
            Log.e("GoogleAuth", "      Current: ${BuildConfig.API_BASE_URL}")
            Log.e("GoogleAuth", "=========================================")
            BackendValidationResult.Error("Connection failed: ${e.message}")
        }
    }

    /**
     * Sends the ID token to the backend API for validation (legacy async method)
     */
    private suspend fun sendTokenToBackend(idToken: String) {
        try {
            Log.d("GoogleAuth", "")
            Log.d("GoogleAuth", "🌐 ========== SENDING TO BACKEND ==========")
            Log.d("GoogleAuth", "📍 Base URL: ${BuildConfig.API_BASE_URL}")
            Log.d("GoogleAuth", "📍 Full Endpoint: ${BuildConfig.API_BASE_URL}api/v1/auth/login")
            Log.d("GoogleAuth", "")
            Log.d("GoogleAuth", "📤 Request Body:")
            Log.d("GoogleAuth", "   {")
            Log.d("GoogleAuth", "     \"id_token\": \"$idToken\"")
            Log.d("GoogleAuth", "   }")
            Log.d("GoogleAuth", "")
            Log.d("GoogleAuth", "📋 ========== COPY TOKEN FOR MANUAL TEST ==========")
            Log.d("GoogleAuth", idToken)
            Log.d("GoogleAuth", "📋 ===============================================")
            Log.d("GoogleAuth", "")

            val request = LoginRequest(idToken = idToken)
            val response = RetrofitClient.authApiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                Log.d("GoogleAuth", "")
                Log.d("GoogleAuth", "✅ ========== BACKEND SUCCESS ==========")
                Log.d("GoogleAuth", "📦 Response code: ${response.code()}")
                Log.d("GoogleAuth", "🔑 Access Token: ${loginResponse?.data?.accessToken?.take(50)}...")
                Log.d("GoogleAuth", "🏷️  Token Type: ${loginResponse?.data?.tokenType}")
                Log.d("GoogleAuth", "👤 Backend User: ${loginResponse?.data?.user?.name} (${loginResponse?.data?.user?.email})")
                Log.d("GoogleAuth", "💬 Message: ${loginResponse?.message}")
                Log.d("GoogleAuth", "==========================================================")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GoogleAuth", "")
                Log.e("GoogleAuth", "❌ ========== BACKEND FAILED ==========")
                Log.e("GoogleAuth", "📦 Response code: ${response.code()}")
                Log.e("GoogleAuth", "📝 Error body: $errorBody")
                Log.e("GoogleAuth", "========================================")
            }
        } catch (e: Exception) {
            Log.e("GoogleAuth", "")
            Log.e("GoogleAuth", "❌ ========== CONNECTION ERROR ==========")
            Log.e("GoogleAuth", "🔍 Exception type: ${e::class.simpleName}")
            Log.e("GoogleAuth", "💬 Error message: ${e.message}")
            Log.e("GoogleAuth", "")
            Log.e("GoogleAuth", "🔧 Troubleshooting:")
            Log.e("GoogleAuth", "   1. Backend running? python manage.py runserver 0.0.0.0:8000")
            Log.e("GoogleAuth", "   2. Same WiFi network?")
            Log.e("GoogleAuth", "   3. Firewall blocking port 8000?")
            Log.e("GoogleAuth", "   4. API_BASE_URL correct in local.properties?")
            Log.e("GoogleAuth", "      Current: ${BuildConfig.API_BASE_URL}")
            Log.e("GoogleAuth", "==========================================")
        }
    }
}

