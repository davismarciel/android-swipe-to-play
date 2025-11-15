package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.auth.TokenManager
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Interceptor that automatically refreshes the token when a 401 Unauthorized response is received.
 * Prevents infinite loops by skipping refresh attempts for the refresh endpoint itself.
 * Uses synchronization to prevent multiple simultaneous refresh attempts.
 */
class TokenRefreshInterceptor(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService
) : Interceptor {

    private val refreshLock = ReentrantLock()
    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        if (response.isSuccessful || response.code != 401) {
            return response
        }

        val requestPath = originalRequest.url.encodedPath
        if (requestPath.contains("/auth/refresh") || requestPath.contains("/auth/login")) {
            ErrorLogger.logDebug("TokenRefreshInterceptor", "Skipping refresh for auth endpoint: $requestPath")
            return response
        }

        val currentToken = tokenManager.getAccessToken()
        if (currentToken == null) {
            ErrorLogger.logWarning("TokenRefreshInterceptor", "No token available for refresh", null)
            return response
        }

        return refreshLock.withLock {
            if (isRefreshing) {
                ErrorLogger.logDebug("TokenRefreshInterceptor", "Refresh already in progress, waiting")
                Thread.sleep(100)
                return@withLock retryRequest(chain, originalRequest)
            }

            isRefreshing = true
            try {
                ErrorLogger.logDebug("TokenRefreshInterceptor", "Attempting to refresh token")
                
                val refreshResponse = runBlocking {
                    authApiService.refresh()
                }

                if (refreshResponse.isSuccessful) {
                    val refreshBody = refreshResponse.body()
                    if (refreshBody?.success == true && refreshBody.data?.accessToken != null) {
                        val newToken = refreshBody.data.accessToken
                        val tokenType = refreshBody.data.tokenType ?: "Bearer"
                        val expiresIn = refreshBody.data.expiresIn

                        tokenManager.saveToken(newToken, tokenType, expiresIn)
                        ErrorLogger.logDebug("TokenRefreshInterceptor", "Token refreshed successfully")

                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "$tokenType $newToken")
                            .build()

                        response.close()
                        return@withLock chain.proceed(newRequest)
                    } else {
                        ErrorLogger.logWarning("TokenRefreshInterceptor", "Refresh response invalid: ${refreshBody?.message}, clearing token", null)
                        tokenManager.clearToken()
                    }
                } else {
                    val errorCode = refreshResponse.code()
                    val errorBody = refreshResponse.errorBody()?.string()
                    ErrorLogger.logWarning("TokenRefreshInterceptor", "Refresh failed: $errorCode - ${refreshResponse.message()}", null)
                    ErrorLogger.logDebug("TokenRefreshInterceptor", "Error body: $errorBody")
                    
                    if (errorCode == 401 || errorCode == 403) {
                        ErrorLogger.logWarning("TokenRefreshInterceptor", "Token refresh failed with auth error, clearing token", null)
                        tokenManager.clearToken()
                    } else {
                        ErrorLogger.logDebug("TokenRefreshInterceptor", "Refresh failed with temporary error ($errorCode), keeping token")
                    }
                }

                response
            } catch (e: java.net.SocketTimeoutException) {
                ErrorLogger.logWarning("TokenRefreshInterceptor", "Network timeout during refresh, keeping token: ${e.message}", e)
                response
            } catch (e: java.net.UnknownHostException) {
                ErrorLogger.logWarning("TokenRefreshInterceptor", "No network connection during refresh, keeping token: ${e.message}", e)
                response
            } catch (e: java.io.IOException) {
                ErrorLogger.logWarning("TokenRefreshInterceptor", "Network error during refresh, keeping token: ${e.message}", e)
                response
            } catch (e: Exception) {
                ErrorLogger.logError("TokenRefreshInterceptor", "Unexpected error during refresh, keeping token: ${e.message}", e)
                response
            } finally {
                isRefreshing = false
            }
        }
    }

    private fun retryRequest(chain: Interceptor.Chain, originalRequest: Request): Response {
        val token = tokenManager.getAuthorizationHeader()
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", token)
                .build()
        } else {
            originalRequest
        }
        return chain.proceed(newRequest)
    }
}

