package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Standard error response from the API
 */
data class ApiErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

