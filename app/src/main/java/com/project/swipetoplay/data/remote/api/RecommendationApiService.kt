package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.RecommendationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface RecommendationApiService {

    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<RecommendationResponse>>

    @GET("api/recommendations/similar/{gameId}")
    suspend fun getSimilarGames(
        @Path("gameId") gameId: Int,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<RecommendationResponse>>

    @GET("api/recommendations/stats")
    suspend fun getRecommendationStats(): Response<ApiResponse<Map<String, Any>>>
}

