package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.UserPreferenceApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.UpdatePreferencesRequest
import com.project.swipetoplay.data.remote.dto.UpdateMonetizationPreferencesRequest
import com.project.swipetoplay.data.remote.dto.UpdatePreferredGenresRequest
import com.project.swipetoplay.data.remote.dto.UpdatePreferredCategoriesRequest
import com.project.swipetoplay.data.remote.dto.GenrePreferenceItem
import com.project.swipetoplay.data.remote.dto.CategoryPreferenceItem
import com.project.swipetoplay.data.remote.dto.UserPreferenceResponse
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger

class UserPreferenceRepository {
    private val apiService: UserPreferenceApiService = RetrofitClient.userPreferenceApiService

    suspend fun getPreferences(): Result<UserPreferenceResponse> {
        return try {
            val response = apiService.getPreferences()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch preferences"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("UserPreferenceRepository", "Failed to fetch preferences: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("UserPreferenceRepository", "Exception while fetching preferences: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    suspend fun updatePreferences(
        preferWindows: Boolean? = null,
        preferMac: Boolean? = null,
        preferLinux: Boolean? = null,
        preferCompetitive: Boolean? = null,
        preferFreeToPlay: Boolean? = null
    ): Result<UserPreferenceResponse> {
        return try {
            val request = UpdatePreferencesRequest(
                preferWindows = preferWindows,
                preferMac = preferMac,
                preferLinux = preferLinux,
                preferCompetitive = preferCompetitive,
                preferFreeToPlay = preferFreeToPlay
            )
            val response = apiService.updatePreferences(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update preferences"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMonetizationPreferences(
        preferOneTimePurchase: Boolean? = null,
        avoidSubscription: Boolean? = null,
        toleranceMicrotransactions: Int? = null,
        toleranceDlc: Int? = null,
        toleranceSeasonPass: Int? = null,
        toleranceLootBoxes: Int? = null,
        toleranceBattlePass: Int? = null,
        toleranceAds: Int? = null,
        tolerancePayToWin: Int? = null,
        preferCosmeticOnly: Boolean? = null
    ): Result<UserPreferenceResponse> {
        return try {
            val request = UpdateMonetizationPreferencesRequest(
                preferOneTimePurchase = preferOneTimePurchase,
                avoidSubscription = avoidSubscription,
                toleranceMicrotransactions = toleranceMicrotransactions,
                toleranceDlc = toleranceDlc,
                toleranceSeasonPass = toleranceSeasonPass,
                toleranceLootBoxes = toleranceLootBoxes,
                toleranceBattlePass = toleranceBattlePass,
                toleranceAds = toleranceAds,
                tolerancePayToWin = tolerancePayToWin,
                preferCosmeticOnly = preferCosmeticOnly
            )
            val response = apiService.updateMonetizationPreferences(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update monetization preferences"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferredGenres(
        genres: List<GenrePreferenceItem>
    ): Result<List<Any>> {
        return try {
            val request = UpdatePreferredGenresRequest(genres = genres)
            val response = apiService.updatePreferredGenres(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update preferred genres"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferredCategories(
        categories: List<CategoryPreferenceItem>
    ): Result<List<Any>> {
        return try {
            val request = UpdatePreferredCategoriesRequest(categories = categories)
            val response = apiService.updatePreferredCategories(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update preferred categories"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

