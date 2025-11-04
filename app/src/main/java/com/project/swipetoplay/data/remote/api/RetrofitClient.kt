package com.project.swipetoplay.data.remote.api

import android.content.Context
import com.project.swipetoplay.BuildConfig
import com.project.swipetoplay.data.auth.TokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    private var tokenManager: TokenManager? = null

    /**
     * Initialize RetrofitClient with context for token management
     * Must be called before any API calls are made
     */
    fun initialize(context: Context) {
        if (tokenManager == null) {
            android.util.Log.d("RetrofitClient", "🔧 Initializing TokenManager")
            tokenManager = TokenManager(context)
        } else {
            android.util.Log.d("RetrofitClient", "⚠️ TokenManager already initialized")
        }
    }

    /**
     * JWT Authentication Interceptor
     * Adds Authorization header to all authenticated requests
     * Note: Accesses tokenManager dynamically at request time, not at creation time
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            if (originalRequest.url.encodedPath.contains("/auth/login")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            val currentTokenManager = tokenManager
            if (currentTokenManager == null) {
                android.util.Log.w("RetrofitClient", "TokenManager not initialized, proceeding without auth")
                return@Interceptor chain.proceed(originalRequest)
            }

            val token = currentTokenManager.getAuthorizationHeader()
            val authenticatedRequest = if (token != null) {
                android.util.Log.d("RetrofitClient", "✅ Adding Authorization header to ${originalRequest.url.encodedPath}")
                android.util.Log.d("RetrofitClient", "   Token preview: ${token.take(30)}...")
                originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
            } else {
                android.util.Log.w("RetrofitClient", "⚠️ No access token found for ${originalRequest.url.encodedPath}, proceeding without auth")
                originalRequest
            }

            chain.proceed(authenticatedRequest)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Create OkHttpClient lazily to ensure tokenManager is initialized
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val gameApiService: GameApiService by lazy {
        retrofit.create(GameApiService::class.java)
    }

    val recommendationApiService: RecommendationApiService by lazy {
        retrofit.create(RecommendationApiService::class.java)
    }

    val interactionApiService: InteractionApiService by lazy {
        retrofit.create(InteractionApiService::class.java)
    }

    val userPreferenceApiService: UserPreferenceApiService by lazy {
        retrofit.create(UserPreferenceApiService::class.java)
    }

    /**
     * Get TokenManager instance
     */
    fun getTokenManager(): TokenManager? = tokenManager
}

