package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.OnboardingCompleteRequest
import com.project.swipetoplay.data.remote.dto.OnboardingStatusResponse
import com.project.swipetoplay.data.remote.dto.RecommendationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API service for onboarding endpoints
 */
interface OnboardingApiService {

    /**
     * Complete onboarding by saving all preferences at once
     */
    @POST("api/onboarding/complete")
    suspend fun completeOnboarding(
        @Body request: OnboardingCompleteRequest
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Get initial recommendations after onboarding
     */
    @GET("api/onboarding/recommendations")
    suspend fun getInitialRecommendations(
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<RecommendationResponse>>

    /**
     * Check onboarding status
     */
    @GET("api/onboarding/status")
    suspend fun checkStatus(): Response<ApiResponse<OnboardingStatusResponse>>
}

