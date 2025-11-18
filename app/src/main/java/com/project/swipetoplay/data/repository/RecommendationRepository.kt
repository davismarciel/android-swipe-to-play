package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.RecommendationApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.remote.dto.RecommendationResponse
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger

class RecommendationRepository {
    private val apiService: RecommendationApiService = RetrofitClient.recommendationApiService

    suspend fun getRecommendations(limit: Int? = null): Result<RecommendationResponse> {
        return try {
            val response = apiService.getRecommendations(limit = limit)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch recommendations"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("RecommendationRepository", "Failed to fetch recommendations: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("RecommendationRepository", "Exception while fetching recommendations: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    suspend fun getSimilarGames(gameId: Int, limit: Int? = null): Result<List<GameResponse>> {
        return try {
            val response = apiService.getSimilarGames(gameId = gameId, limit = limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data.recommendations)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch similar games"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("RecommendationRepository", "Failed to fetch similar games: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("RecommendationRepository", "Exception while fetching similar games: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    suspend fun getRecommendationStats(): Result<Map<String, Any>> {
        return try {
            val response = apiService.getRecommendationStats()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch stats"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("RecommendationRepository", "Failed to fetch recommendation stats: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("RecommendationRepository", "Exception while fetching recommendation stats: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }
}

