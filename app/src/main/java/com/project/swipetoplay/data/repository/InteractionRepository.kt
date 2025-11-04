package com.project.swipetoplay.data.repository

import com.project.swipetoplay.data.remote.api.InteractionApiService
import com.project.swipetoplay.data.remote.api.RetrofitClient

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
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

