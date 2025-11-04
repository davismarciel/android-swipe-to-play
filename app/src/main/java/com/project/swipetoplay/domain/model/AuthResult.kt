package com.project.swipetoplay.domain.model

/**
 * Sealed class representing the result of an authentication attempt.
 */
sealed class AuthResult {
    /**
     * Authentication was successful.
     *
     * @property user The authenticated Google user
     */
    data class Success(val user: GoogleUser) : AuthResult()

    /**
     * Authentication failed with an error.
     *
     * @property message Error message describing what went wrong
     */
    data class Error(val message: String) : AuthResult()

    /**
     * Authentication was cancelled by the user.
     */
    data object Cancelled : AuthResult()
}

