package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.InteractionApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.data.error.ErrorHandler
import com.project.swipetoplay.data.error.ErrorLogger

/**
 * Repository for game interaction operations (like, dislike, view)
 * These interactions are sent to the backend for algorithm training only,
 * not visible to the user.
 */
class InteractionRepository {
    private val apiService: InteractionApiService = RetrofitClient.interactionApiService

    /**
     * Record a like interaction for a game (silently, for algorithm)
     */
    suspend fun likeGame(gameId: Int): Result<Unit> {
        return try {
            val response = apiService.likeGame(gameId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("InteractionRepository", "Failed to record like: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("InteractionRepository", "Exception while recording like: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Record a dislike interaction for a game (silently, for algorithm)
     */
    suspend fun dislikeGame(gameId: Int): Result<Unit> {
        return try {
            val response = apiService.dislikeGame(gameId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("InteractionRepository", "Failed to record dislike: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("InteractionRepository", "Exception while recording dislike: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Record a view interaction for a game
     */
    suspend fun viewGame(gameId: Int): Result<Unit> {
        return try {
            val response = apiService.viewGame(gameId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("InteractionRepository", "Failed to record view: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("InteractionRepository", "Exception while recording view: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }

    /**
     * Clear all interactions for the current user (DEV ONLY)
     */
    suspend fun clearAllInteractions(): Result<Unit> {
        return try {
            val response = apiService.clearAllInteractions()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = ErrorHandler.getUserFriendlyMessage(response.code(), response.message())
                ErrorLogger.logError("InteractionRepository", "Failed to clear interactions: HTTP ${response.code()}", null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            ErrorLogger.logError("InteractionRepository", "Exception while clearing interactions: ${e.message}", e)
            Result.failure(Exception(ErrorHandler.getUserFriendlyMessage(e)))
        }
    }
}

