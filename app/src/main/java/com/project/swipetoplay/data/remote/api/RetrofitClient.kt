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

    fun initialize(context: Context) {
        if (tokenManager == null) {
            com.project.swipetoplay.data.error.ErrorLogger.logDebug("RetrofitClient", "Initializing TokenManager")
            tokenManager = TokenManager(context)
        } else {
            com.project.swipetoplay.data.error.ErrorLogger.logDebug("RetrofitClient", "TokenManager already initialized")
        }
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            if (originalRequest.url.encodedPath.contains("/auth/login") || 
                originalRequest.url.encodedPath.contains("/auth/health")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            val currentTokenManager = tokenManager
            if (currentTokenManager == null) {
                com.project.swipetoplay.data.error.ErrorLogger.logWarning("RetrofitClient", "TokenManager not initialized, proceeding without auth", null)
                return@Interceptor chain.proceed(originalRequest)
            }

            val token = currentTokenManager.getAuthorizationHeader()
            val authenticatedRequest = if (token != null) {
                com.project.swipetoplay.data.error.ErrorLogger.logDebug("RetrofitClient", "Adding Authorization header to ${originalRequest.url.encodedPath}")
                com.project.swipetoplay.data.error.ErrorLogger.logDebug("RetrofitClient", "Token preview: ${token.take(30)}...")
                originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
            } else {
                com.project.swipetoplay.data.error.ErrorLogger.logWarning("RetrofitClient", "No access token found for ${originalRequest.url.encodedPath}, proceeding without auth", null)
                originalRequest
            }

            chain.proceed(authenticatedRequest)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val basicOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val basicRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(basicOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val authApiServiceForRefresh: AuthApiService by lazy {
        basicRetrofit.create(AuthApiService::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val currentTokenManager = tokenManager
            ?: throw IllegalStateException("TokenManager not initialized. Call RetrofitClient.initialize(context) first.")

        OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addNetworkInterceptor(TokenRefreshInterceptor(currentTokenManager, authApiServiceForRefresh))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
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

    val onboardingApiService: OnboardingApiService by lazy {
        retrofit.create(OnboardingApiService::class.java)
    }

    fun getTokenManager(): TokenManager? = tokenManager
}

