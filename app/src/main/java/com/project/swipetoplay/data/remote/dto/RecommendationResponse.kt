package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class RecommendationResponse(
    @SerializedName("recommendations")
    val recommendations: List<GameResponse>,

    @SerializedName("count")
    val count: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("message")
    val message: String? = null
)

