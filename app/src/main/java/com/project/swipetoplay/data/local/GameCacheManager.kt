package com.project.swipetoplay.data.local

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.domain.mapper.GameMapper
import com.project.swipetoplay.ui.features.game.Game
import com.project.swipetoplay.data.error.ErrorLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages caching of games and preloading of images
 * Preloads 20 games (default daily limit) when app starts
 */
class GameCacheManager(
    private val context: Context,
    private val recommendationRepository: RecommendationRepository
) {
    private var cachedGames: List<Game>? = null
    private var isPreloading = false
    private val imageLoader = ImageLoader(context)

    /**
     * Get cached games if available
     */
    fun getCachedGames(): List<Game>? {
        return cachedGames
    }

    /**
     * Check if games are cached
     */
    fun hasCachedGames(): Boolean {
        return cachedGames != null && cachedGames!!.isNotEmpty()
    }

    /**
     * Check if preloading is in progress
     */
    fun isPreloading(): Boolean {
        return isPreloading
    }

    /**
     * Preload 20 games and their images
     * Should be called when app starts and user is authenticated
     */
    fun preloadGames(
        scope: CoroutineScope,
        onSuccess: ((List<Game>) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        if (isPreloading) {
            ErrorLogger.logDebug("GameCacheManager", "Preload already in progress")
            return
        }

        if (hasCachedGames()) {
            ErrorLogger.logDebug("GameCacheManager", "Games already cached, skipping preload")
            cachedGames?.let { onSuccess?.invoke(it) }
            return
        }

        isPreloading = true
        ErrorLogger.logDebug("GameCacheManager", "Starting game preload")

        scope.launch(Dispatchers.IO) {
            try {
                val result = recommendationRepository.getRecommendations(limit = 20)

                result.fold(
                    onSuccess = { response ->
                        val games = GameMapper.toGameList(response.recommendations)
                        ErrorLogger.logDebug(
                            "GameCacheManager",
                            "Cached payload - remaining today: ${response.dailyLimitInfo?.remainingToday ?: "unknown"}"
                        )

                        preloadImages(games)

                        withContext(Dispatchers.Main) {
                            cachedGames = games
                            isPreloading = false
                            ErrorLogger.logDebug("GameCacheManager", "Successfully cached ${games.size} games")
                            onSuccess?.invoke(games)
                        }
                    },
                    onFailure = { throwable ->
                        withContext(Dispatchers.Main) {
                            isPreloading = false
                            val exception = throwable as? Exception ?: Exception(throwable.message, throwable)
                            ErrorLogger.logError("GameCacheManager", "Failed to preload games: ${exception.message}", exception)
                            onError?.invoke(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isPreloading = false
                    ErrorLogger.logError("GameCacheManager", "Exception during preload: ${e.message}", e)
                    onError?.invoke(e)
                }
            }
        }
    }

    /**
     * Preload images for all games using Coil
     */
    private suspend fun preloadImages(games: List<Game>) {
        withContext(Dispatchers.IO) {
            val imageUrls = games.mapNotNull { game ->
                game.getSteamImageUrl()
            }

            ErrorLogger.logDebug("GameCacheManager", "Preloading ${imageUrls.size} images")

            imageUrls.forEach { imageUrl ->
                try {
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build()
                    
                    imageLoader.enqueue(request)
                } catch (e: Exception) {
                    ErrorLogger.logWarning("GameCacheManager", "Failed to preload image: $imageUrl - ${e.message}", e)
                }
            }

            ErrorLogger.logDebug("GameCacheManager", "Image preloading initiated for ${imageUrls.size} images")
        }
    }

    /**
     * Clear the cache
     */
    fun clearCache() {
        cachedGames = null
        val prefs = context.getSharedPreferences("swipe_to_play_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("cache_was_cleared", true)
            .apply()
        ErrorLogger.logDebug("GameCacheManager", "Cache cleared")
    }
    
    /**
     * Check if cache was cleared and reset the flag
     */
    fun wasCacheCleared(): Boolean {
        val prefs = context.getSharedPreferences("swipe_to_play_prefs", Context.MODE_PRIVATE)
        val wasCleared = prefs.getBoolean("cache_was_cleared", false)
        if (wasCleared) {
            prefs.edit().remove("cache_was_cleared").apply()
        }
        return wasCleared
    }
}

