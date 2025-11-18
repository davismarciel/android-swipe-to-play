package com.project.swipetoplay.data.preferences

import android.content.Context
import android.content.SharedPreferences


class NotificationPreferences(context: Context) {
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SWIPE_READY_NOTIFICATION = "swipe_ready_notification"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
        private const val KEY_EMAIL_NOTIFICATIONS = "email_notifications"
        
        private const val DEFAULT_SWIPE_READY = true
        private const val DEFAULT_SOUND = true
        private const val DEFAULT_VIBRATION = true
        private const val DEFAULT_PUSH = true
        private const val DEFAULT_EMAIL = false
    }
    
    var swipeReadyNotification: Boolean
        get() = sharedPrefs.getBoolean(KEY_SWIPE_READY_NOTIFICATION, DEFAULT_SWIPE_READY)
        set(value) = sharedPrefs.edit().putBoolean(KEY_SWIPE_READY_NOTIFICATION, value).apply()
    
    var soundEnabled: Boolean
        get() = sharedPrefs.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND)
        set(value) = sharedPrefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
    
    var vibrationEnabled: Boolean
        get() = sharedPrefs.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION)
        set(value) = sharedPrefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
    
    var pushNotifications: Boolean
        get() = sharedPrefs.getBoolean(KEY_PUSH_NOTIFICATIONS, DEFAULT_PUSH)
        set(value) = sharedPrefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, value).apply()
    
    var emailNotifications: Boolean
        get() = sharedPrefs.getBoolean(KEY_EMAIL_NOTIFICATIONS, DEFAULT_EMAIL)
        set(value) = sharedPrefs.edit().putBoolean(KEY_EMAIL_NOTIFICATIONS, value).apply()
    
    
    fun saveAll(
        swipeReady: Boolean,
        sound: Boolean,
        vibration: Boolean,
        push: Boolean,
        email: Boolean
    ) {
        sharedPrefs.edit().apply {
            putBoolean(KEY_SWIPE_READY_NOTIFICATION, swipeReady)
            putBoolean(KEY_SOUND_ENABLED, sound)
            putBoolean(KEY_VIBRATION_ENABLED, vibration)
            putBoolean(KEY_PUSH_NOTIFICATIONS, push)
            putBoolean(KEY_EMAIL_NOTIFICATIONS, email)
            apply()
        }
    }
    
    
    fun resetToDefaults() {
        saveAll(
            swipeReady = DEFAULT_SWIPE_READY,
            sound = DEFAULT_SOUND,
            vibration = DEFAULT_VIBRATION,
            push = DEFAULT_PUSH,
            email = DEFAULT_EMAIL
        )
    }
}
