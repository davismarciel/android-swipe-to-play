package com.project.swipetoplay.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.project.swipetoplay.data.error.ErrorLogger

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

    fun saveToken(accessToken: String, tokenType: String = "Bearer", expiresIn: Int? = null) {
        ErrorLogger.logDebug("TokenManager", "Saving new token (length: ${accessToken.length}, type: $tokenType)")
        
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .putInt(KEY_EXPIRES_IN, expiresIn ?: 0)
            .apply()
        
        cachedAccessToken = accessToken
        cachedTokenType = tokenType
        isCacheValid = true
        
        ErrorLogger.logDebug("TokenManager", "Token saved and cache updated")
    }

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

    fun getTokenType(): String {
        if (isCacheValid && cachedTokenType != null) {
            return cachedTokenType!!
        }
        
        val type = prefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
        cachedTokenType = type
        return type
    }

    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    fun clearToken() {
        ErrorLogger.logDebug("TokenManager", "Clearing token")
        
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_TOKEN_TYPE)
            .remove(KEY_EXPIRES_IN)
            .apply()
        
        cachedAccessToken = null
        cachedTokenType = null
        isCacheValid = false
        
        ErrorLogger.logDebug("TokenManager", "Token cleared and cache invalidated")
    }

    fun getAuthorizationHeader(): String? {
        val token = getAccessToken() ?: return null
        val type = getTokenType()
        val normalizedType = if (type.equals("bearer", ignoreCase = true)) "Bearer" else type
        return "$normalizedType $token"
    }
}

