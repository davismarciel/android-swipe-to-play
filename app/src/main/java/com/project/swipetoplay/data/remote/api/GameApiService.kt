package com.project.swipetoplay.data.remote.api

import com.project.swipetoplay.data.remote.dto.ApiResponse
import com.project.swipetoplay.data.remote.dto.GameResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service for game-related endpoints
 */
interface GameApiService {

    @GET("api/games")
    suspend fun getGames(
        @Query("search") search: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("is_free") isFree: Boolean? = null,
        @Query("platform") platform: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiResponse<List<GameResponse>>>

    @GET("api/games/{id}")
    suspend fun getGameById(
        @Path("id") id: Int
    ): Response<ApiResponse<GameResponse>>

    @GET("api/genres")
    suspend fun getGenres(): Response<ApiResponse<List<com.project.swipetoplay.data.remote.dto.GenreResponse>>>

    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<List<com.project.swipetoplay.data.remote.dto.CategoryResponse>>>
}

