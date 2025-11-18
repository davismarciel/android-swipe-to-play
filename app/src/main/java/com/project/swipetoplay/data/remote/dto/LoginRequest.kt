package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String
)

