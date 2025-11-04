package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.LoginRequest
import com.project.swipetoplay.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API service for authentication endpoints
 */
interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}

