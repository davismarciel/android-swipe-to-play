package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.RecommendationApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.remote.dto.RecommendationResponse

/**
 * Repository for recommendation-related data operations
 */
class RecommendationRepository {
    private val apiService: RecommendationApiService = RetrofitClient.recommendationApiService

    /**
     * Get personalized game recommendations
     */
    suspend fun getRecommendations(limit: Int? = null): Result<List<GameResponse>> {
        return try {
            val response = apiService.getRecommendations(limit = limit)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data.recommendations)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch recommendations"))
                }
            } else {
                when (response.code()) {
                    401 -> {
                        android.util.Log.w("RecommendationRepository", "⚠️ Unauthenticated - Token invalid or expired")
                        Result.failure(Exception("Token invalid or expired. Please login again."))
                    }
                    500 -> {
                        android.util.Log.e("RecommendationRepository", "❌ Server error: ${response.message()}")
                        Result.failure(Exception("Internal server error. Please try again later."))
                    }
                    else -> {
                        android.util.Log.w("RecommendationRepository", "⚠️ HTTP ${response.code()}: ${response.message()}")
                        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RecommendationRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get similar games to a specific game
     */
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
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recommendation statistics for the current user
     */
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
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

