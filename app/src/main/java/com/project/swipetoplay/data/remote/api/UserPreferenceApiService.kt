package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.UpdatePreferencesRequest
import com.project.swipetoplay.data.remote.dto.UpdateMonetizationPreferencesRequest
import com.project.swipetoplay.data.remote.dto.UpdatePreferredGenresRequest
import com.project.swipetoplay.data.remote.dto.UpdatePreferredCategoriesRequest
import com.project.swipetoplay.data.remote.dto.UserPreferenceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body

/**
 * API service for user preference endpoints
 */
interface UserPreferenceApiService {

    @GET("api/user/preferences")
    suspend fun getPreferences(): Response<ApiResponse<UserPreferenceResponse>>

    @PUT("api/user/preferences")
    suspend fun updatePreferences(
        @Body preferences: UpdatePreferencesRequest
    ): Response<ApiResponse<UserPreferenceResponse>>

    @PUT("api/user/preferences/monetization")
    suspend fun updateMonetizationPreferences(
        @Body preferences: UpdateMonetizationPreferencesRequest
    ): Response<ApiResponse<UserPreferenceResponse>>

    @PUT("api/user/preferences/genres")
    suspend fun updatePreferredGenres(
        @Body request: UpdatePreferredGenresRequest
    ): Response<ApiResponse<List<Any>>>

    @PUT("api/user/preferences/categories")
    suspend fun updatePreferredCategories(
        @Body request: UpdatePreferredCategoriesRequest
    ): Response<ApiResponse<List<Any>>>
}

