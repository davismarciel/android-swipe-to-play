package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * API service for game interaction endpoints (like, dislike, view, etc.)
 * These interactions are used by the recommendation algorithm only,
 * not visible to the user.
 */
interface InteractionApiService {

    @POST("api/games/{gameId}/like")
    suspend fun likeGame(
        @Path("gameId") gameId: Int
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("api/games/{gameId}/dislike")
    suspend fun dislikeGame(
        @Path("gameId") gameId: Int
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("api/games/{gameId}/view")
    suspend fun viewGame(
        @Path("gameId") gameId: Int
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Delete all interactions for the current user (DEV ONLY)
     */
    @DELETE("api/interactions/clear")
    suspend fun clearAllInteractions(): Response<ApiResponse<Map<String, Any>>>
}

