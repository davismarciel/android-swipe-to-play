package com.project.swipetoplay.data.error

import android.util.Log

object ErrorLogger {
    fun logError(tag: String, message: String, exception: Throwable? = null) {
        val cleanMessage = removeEmojis(message)
        if (exception != null) {
            Log.e(tag, "$cleanMessage | exception: ${exception.javaClass.simpleName}", exception)
        } else {
            Log.e(tag, cleanMessage)
        }
    }

    fun logWarning(tag: String, message: String, exception: Throwable? = null) {
        val cleanMessage = removeEmojis(message)
        if (exception != null) {
            Log.w(tag, "$cleanMessage | exception: ${exception.javaClass.simpleName}", exception)
        } else {
            Log.w(tag, cleanMessage)
        }
    }

    fun logDebug(tag: String, message: String) {
        val cleanMessage = removeEmojis(message)
        Log.d(tag, cleanMessage)
    }

    fun logInfo(tag: String, message: String) {
        val cleanMessage = removeEmojis(message)
        Log.i(tag, cleanMessage)
    }

    private fun removeEmojis(text: String): String {
        return text.replace(Regex("[\uD83C-\uDBFF\uDC00-\uDFFF]+"), "")
            .replace(Regex("[\\u2600-\\u27BF]+"), "")
            .trim()
    }
}

