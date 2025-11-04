package com.project.swipetoplay.data.local

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.domain.mapper.GameMapper
import com.project.swipetoplay.ui.features.game.Game
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
            android.util.Log.d("GameCacheManager", "⚠️ Preload already in progress")
            return
        }

        if (hasCachedGames()) {
            android.util.Log.d("GameCacheManager", "✅ Games already cached, skipping preload")
            cachedGames?.let { onSuccess?.invoke(it) }
            return
        }

        isPreloading = true
        android.util.Log.d("GameCacheManager", "🔄 Starting game preload...")

        scope.launch(Dispatchers.IO) {
            try {
                val result = recommendationRepository.getRecommendations(limit = 20)

                result.fold(
                    onSuccess = { gameResponses ->
                        val games = GameMapper.toGameList(gameResponses)
                        
                        // Preload images for all games
                        preloadImages(games)
                        
                        withContext(Dispatchers.Main) {
                            cachedGames = games
                            isPreloading = false
                            android.util.Log.d("GameCacheManager", "✅ Successfully cached ${games.size} games")
                            onSuccess?.invoke(games)
                        }
                    },
                    onFailure = { throwable ->
                        withContext(Dispatchers.Main) {
                            isPreloading = false
                            val exception = throwable as? Exception ?: Exception(throwable.message, throwable)
                            android.util.Log.e("GameCacheManager", "❌ Failed to preload games: ${exception.message}", exception)
                            onError?.invoke(exception)
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isPreloading = false
                    android.util.Log.e("GameCacheManager", "❌ Exception during preload: ${e.message}", e)
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

            android.util.Log.d("GameCacheManager", "🖼️ Preloading ${imageUrls.size} images...")

            imageUrls.forEach { imageUrl ->
                try {
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build()
                    
                    imageLoader.enqueue(request)
                } catch (e: Exception) {
                    android.util.Log.w("GameCacheManager", "⚠️ Failed to preload image: $imageUrl - ${e.message}")
                }
            }

            android.util.Log.d("GameCacheManager", "✅ Image preloading initiated for ${imageUrls.size} images")
        }
    }

    /**
     * Clear the cache
     */
    fun clearCache() {
        cachedGames = null
        android.util.Log.d("GameCacheManager", "🗑️ Cache cleared")
    }
}

