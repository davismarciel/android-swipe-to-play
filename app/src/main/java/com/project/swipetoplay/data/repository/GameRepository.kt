package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.GameApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger
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
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("GameRepository", "Failed to fetch games: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GameRepository", "Exception while fetching games: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Get game details by ID
     */
    suspend fun getGameById(id: Int): Result<GameResponse> {
        return try {
            ErrorLogger.logDebug("GameRepository", "Fetching game by ID: $id")
            val response = apiService.getGameById(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                ErrorLogger.logDebug("GameRepository", "Response success: ${body?.success}")

                if (body?.success == true && body.data != null) {
                    val game = body.data
                    ErrorLogger.logDebug("GameRepository", "Game name: ${game.name}")
                    Result.success(game)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to fetch game details"))
                }
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("GameRepository", "Failed to fetch game: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GameRepository", "Error fetching game: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
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
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("GameRepository", "Failed to fetch genres: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GameRepository", "Exception loading genres: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
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
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("GameRepository", "Failed to fetch categories: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("GameRepository", "Exception loading categories: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }
}

