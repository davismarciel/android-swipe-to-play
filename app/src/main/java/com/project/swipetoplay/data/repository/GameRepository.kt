package com.project.swipetoplay.data.repository

import android.util.Log
import com.project.swipetoplay.data.remote.api.GameApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.GameResponse
import retrofit2.Response

/**
 * Repository for game-related data operations
 */
class GameRepository {
    private val apiService: GameApiService = RetrofitClient.gameApiService

    /**
     * Get list of games with optional filters
     */
    suspend fun getGames(
        search: String? = null,
        genreId: Int? = null,
        categoryId: Int? = null,
        isFree: Boolean? = null,
        platform: String? = null,
        perPage: Int? = null
    ): Result<List<GameResponse>> {
        return try {
            val response = apiService.getGames(
                search = search,
                genreId = genreId,
                categoryId = categoryId,
                isFree = isFree,
                platform = platform,
                perPage = perPage
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch games"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get game details by ID
     */
    suspend fun getGameById(id: Int): Result<GameResponse> {
        return try {
            Log.d("GameRepository", "=== FETCHING GAME BY ID: $id ===")
            val response = apiService.getGameById(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("GameRepository", "Response success: ${body?.success}")
                Log.d("GameRepository", "Response message: ${body?.message}")

                if (body?.success == true && body.data != null) {
                    val game = body.data
                    Log.d("GameRepository", "Game name: ${game.name}")
                    Log.d("GameRepository", "Raw communityRating from API: ${game.communityRating}")
                    game.communityRating?.let { rating ->
                        Log.d("GameRepository", "  Raw toxicity: ${rating.toxicity}")
                        Log.d("GameRepository", "  Raw bugs: ${rating.bugs}")
                        Log.d("GameRepository", "  Raw microtransactions: ${rating.microtransactions}")
                        Log.d("GameRepository", "  Raw optimization: ${rating.optimization}")
                        Log.d("GameRepository", "  Raw cheaters: ${rating.cheaters}")
                    }
                    Result.success(game)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch game"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("GameRepository", "Error fetching game: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all available genres
     */
    suspend fun getGenres(): Result<List<com.project.swipetoplay.data.remote.dto.GenreResponse>> {
        return try {
            val response = apiService.getGenres()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch genres"))
                }
            } else {
                when (response.code()) {
                    401 -> {
                        android.util.Log.w("GameRepository", "⚠️ Unauthenticated - Token invalid or expired")
                        Result.failure(Exception("Token invalid or expired. Please login again."))
                    }
                    500 -> {
                        android.util.Log.e("GameRepository", "❌ Server error: ${response.message()}")
                        Result.failure(Exception("Internal server error. Please try again later."))
                    }
                    else -> {
                        android.util.Log.w("GameRepository", "⚠️ HTTP ${response.code()}: ${response.message()}")
                        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GameRepository", "❌ Exception loading genres: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all available categories
     */
    suspend fun getCategories(): Result<List<com.project.swipetoplay.data.remote.dto.CategoryResponse>> {
        return try {
            val response = apiService.getCategories()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch categories"))
                }
            } else {
                when (response.code()) {
                    401 -> {
                        android.util.Log.w("GameRepository", "⚠️ Unauthenticated - Token invalid or expired")
                        Result.failure(Exception("Token invalid or expired. Please login again."))
                    }
                    500 -> {
                        android.util.Log.e("GameRepository", "❌ Server error: ${response.message()}")
                        Result.failure(Exception("Internal server error. Please try again later."))
                    }
                    else -> {
                        android.util.Log.w("GameRepository", "⚠️ HTTP ${response.code()}: ${response.message()}")
                        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GameRepository", "❌ Exception loading categories: ${e.message}", e)
            Result.failure(e)
        }
    }
}

