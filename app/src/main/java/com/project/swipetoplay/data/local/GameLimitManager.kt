package com.project.swipetoplay.data.local

import android.content.Context
import android.content.SharedPreferences
import com.project.swipetoplay.data.error.ErrorLogger
import java.util.Calendar

/**
 * Manages daily game limit for users
 * Tracks how many games a user has viewed/interacted with per day
 */
class GameLimitManager(private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "swipe_to_play_prefs"
        private const val KEY_GAME_COUNT = "daily_game_count"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val DEFAULT_DAILY_LIMIT = 20
    }

    /**
     * Get the daily limit (configurable, default is 20)
     */
    fun getDailyLimit(): Int {
        return DEFAULT_DAILY_LIMIT
    }

    /**
     * Get current count of games viewed today
     */
    fun getCurrentCount(): Int {
        checkAndResetIfNeeded()
        return prefs.getInt(KEY_GAME_COUNT, 0)
    }

    /**
     * Check if user has reached daily limit
     */
    fun hasReachedLimit(): Boolean {
        return getCurrentCount() >= getDailyLimit()
    }

    /**
     * Get remaining games for today
     */
    fun getRemainingGames(): Int {
        checkAndResetIfNeeded()
        val currentCount = getCurrentCount()
        val dailyLimit = getDailyLimit()
        val remaining = dailyLimit - currentCount
        val result = remaining.coerceAtLeast(0)
        
        ErrorLogger.logDebug("GameLimitManager", "Remaining games calculation: Limit=$dailyLimit, Count=$currentCount, Remaining=$result")
        return result
    }

    /**
     * Increment game count (when user likes a game)
     * ONLY called when user swipes right (likes)
     * Dislikes and views do NOT increment the count
     */
    fun incrementCount() {
        checkAndResetIfNeeded()
        val currentCount = getCurrentCount()
        
        if (currentCount >= getDailyLimit()) {
            ErrorLogger.logWarning("GameLimitManager", "Attempted to increment count but already at limit: $currentCount/${getDailyLimit()}", null)
            return
        }
        
        val newCount = currentCount + 1
        prefs.edit()
            .putInt(KEY_GAME_COUNT, newCount)
            .apply()
        
        ErrorLogger.logDebug("GameLimitManager", "Count incremented (LIKE ONLY): $currentCount -> $newCount (Limit: ${getDailyLimit()}, Remaining: ${getDailyLimit() - newCount})")
    }

    /**
     * Check if a new day has started and reset count if needed
     */
    private fun checkAndResetIfNeeded() {
        val lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, null)
        val today = getTodayDateString()

        if (lastResetDate != today) {
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_GAME_COUNT, 0)
                .apply()
        }
    }

    /**
     * Get today's date as string (YYYY-MM-DD)
     */
    private fun getTodayDateString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    /**
     * Manually reset count (for testing or admin purposes)
     */
    fun resetCount() {
        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, getTodayDateString())
            .putInt(KEY_GAME_COUNT, 0)
            .apply()
    }
}

