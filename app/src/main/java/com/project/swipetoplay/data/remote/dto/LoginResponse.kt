package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: LoginData? = null,

    @SerializedName("message")
    val message: String? = null
)

data class LoginData(
    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("token_type")
    val tokenType: String? = null,

    @SerializedName("user")
    val user: UserDto? = null,

    @SerializedName("expires_in")
    val expiresIn: Int? = null
)

data class UserDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("avatar")
    val avatar: String?
)

