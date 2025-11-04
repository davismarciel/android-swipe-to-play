package com.project.swipetoplay.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages JWT token storage and retrieval
 */
class TokenManager(private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    @Volatile
    private var cachedAccessToken: String? = null
    
    @Volatile
    private var cachedTokenType: String? = null
    
    @Volatile
    private var isCacheValid: Boolean = false

    companion object {
        private const val PREFS_NAME = "swipe_to_play_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_IN = "expires_in"
    }

    /**
     * Save JWT token to shared preferences
     */
    fun saveToken(accessToken: String, tokenType: String = "Bearer", expiresIn: Int? = null) {
        android.util.Log.d("TokenManager", "💾 Saving new token (length: ${accessToken.length}, type: $tokenType)")
        
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .putInt(KEY_EXPIRES_IN, expiresIn ?: 0)
            .apply()
        
        cachedAccessToken = accessToken
        cachedTokenType = tokenType
        isCacheValid = true
        
        android.util.Log.d("TokenManager", "✅ Token saved and cache updated")
    }

    /**
     * Get stored access token
     * Uses in-memory cache to avoid reading from SharedPreferences on every request
     */
    fun getAccessToken(): String? {
        if (isCacheValid && cachedAccessToken != null) {
            return cachedAccessToken
        }
        
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        if (token != null) {
            cachedAccessToken = token
            cachedTokenType = prefs.getString(KEY_TOKEN_TYPE, "Bearer")
            isCacheValid = true
        }
        return token
    }

    /**
     * Get token type (usually "Bearer")
     */
    fun getTokenType(): String {
        if (isCacheValid && cachedTokenType != null) {
            return cachedTokenType!!
        }
        
        val type = prefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
        cachedTokenType = type
        return type
    }

    /**
     * Check if user is authenticated (has valid token)
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Clear stored token (logout)
     */
    fun clearToken() {
        android.util.Log.d("TokenManager", "🗑️ Clearing token")
        
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_TOKEN_TYPE)
            .remove(KEY_EXPIRES_IN)
            .apply()
        
        cachedAccessToken = null
        cachedTokenType = null
        isCacheValid = false
        
        android.util.Log.d("TokenManager", "✅ Token cleared and cache invalidated")
    }

    /**
     * Get full authorization header value
     * Ensures "Bearer" (capital B) format for Laravel JWT compatibility
     */
    fun getAuthorizationHeader(): String? {
        val token = getAccessToken() ?: return null
        val type = getTokenType()
        val normalizedType = if (type.equals("bearer", ignoreCase = true)) "Bearer" else type
        return "$normalizedType $token"
    }
}

