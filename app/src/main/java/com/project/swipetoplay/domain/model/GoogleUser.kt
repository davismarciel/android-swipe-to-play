package com.project.swipetoplay.domain.model

/**
 * Data class representing a Google authenticated user.
 *
 * @property id Unique identifier for the user from Google
 * @property email User's email address
 * @property displayName User's display name
 * @property profilePictureUrl URL to the user's profile picture
 */
data class GoogleUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val profilePictureUrl: String?
)

