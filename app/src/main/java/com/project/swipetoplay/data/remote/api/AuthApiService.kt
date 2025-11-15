package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.LoginRequest
import com.project.swipetoplay.data.remote.dto.LoginResponse
import com.project.swipetoplay.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API service for authentication endpoints
 */
interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Refresh the access token using the current (possibly expired) token
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    /**
     * Get current authenticated user information
     */
    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<Map<String, Any>>>
}

