package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

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
}

