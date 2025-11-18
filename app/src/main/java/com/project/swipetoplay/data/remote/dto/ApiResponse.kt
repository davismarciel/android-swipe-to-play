package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("from")
    val from: Int? = null,

    @SerializedName("last_page")
    val lastPage: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("to")
    val to: Int? = null,

    @SerializedName("total")
    val total: Int
)

