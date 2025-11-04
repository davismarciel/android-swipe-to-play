package com.project.swipetoplay.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages onboarding completion status
 */
class OnboardingManager(private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "swipe_to_play_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    /**
     * Check if user has completed onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Mark onboarding as completed
     */
    fun completeOnboarding() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    /**
     * Reset onboarding (for testing or logout)
     */
    fun resetOnboarding() {
        prefs.edit()
            .remove(KEY_ONBOARDING_COMPLETED)
            .apply()
    }
}

