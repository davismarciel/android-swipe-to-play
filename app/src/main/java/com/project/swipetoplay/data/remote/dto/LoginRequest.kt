package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for Google Sign-In authentication
 */
data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String
)

