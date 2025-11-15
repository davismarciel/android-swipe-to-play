package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.OnboardingApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.remote.dto.OnboardingCompleteRequest
import com.project.swipetoplay.data.remote.dto.OnboardingStatusResponse
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger

/**
 * Repository for onboarding-related data operations
 */
class OnboardingRepository {
    private val apiService: OnboardingApiService = RetrofitClient.onboardingApiService

    /**
     * Complete onboarding by saving all preferences at once
     */
    suspend fun completeOnboarding(request: OnboardingCompleteRequest): Result<Map<String, Any>> {
        return try {
            ErrorLogger.logDebug("OnboardingRepository", "Sending onboarding data to API")
            val response = apiService.completeOnboarding(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    ErrorLogger.logDebug("OnboardingRepository", "Onboarding completed successfully")
                    Result.success(body.data)
                } else {
                    ErrorLogger.logWarning("OnboardingRepository", "API returned error: ${body?.message}")
                    Result.failure(Exception(body?.message ?: "Failed to complete onboarding"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("OnboardingRepository", "Failed to complete onboarding: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("OnboardingRepository", "Exception while completing onboarding: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Get initial recommendations after onboarding
     */
    suspend fun getInitialRecommendations(limit: Int? = 20): Result<List<GameResponse>> {
        return try {
            ErrorLogger.logDebug("OnboardingRepository", "Fetching initial recommendations (limit: $limit)")
            val response = apiService.getInitialRecommendations(limit = limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    ErrorLogger.logDebug("OnboardingRepository", "Received ${body.data.recommendations.size} recommendations")
                    Result.success(body.data.recommendations)
                } else {
                    ErrorLogger.logWarning("OnboardingRepository", "API returned error: ${body?.message}")
                    Result.failure(Exception(body?.message ?: "Failed to fetch initial recommendations"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("OnboardingRepository", "Failed to fetch initial recommendations: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("OnboardingRepository", "Exception while fetching initial recommendations: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Check onboarding status
     */
    suspend fun checkStatus(): Result<OnboardingStatusResponse> {
        return try {
            ErrorLogger.logDebug("OnboardingRepository", "Checking onboarding status")
            val response = apiService.checkStatus()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    ErrorLogger.logDebug("OnboardingRepository", "Status: completed=${body.data.completed}")
                    Result.success(body.data)
                } else {
                    ErrorLogger.logWarning("OnboardingRepository", "API returned error: ${body?.message}")
                    Result.failure(Exception(body?.message ?: "Failed to check status"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("OnboardingRepository", "Failed to check status: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("OnboardingRepository", "Exception while checking status: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }
}

