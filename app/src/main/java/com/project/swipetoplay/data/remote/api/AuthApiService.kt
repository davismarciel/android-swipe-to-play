package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.LoginRequest
import com.project.swipetoplay.data.remote.dto.LoginResponse
import com.project.swipetoplay.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<Map<String, Any>>>

    @GET("api/v1/auth/health")
    suspend fun health(): Response<ApiResponse<Map<String, Any>>>
}

