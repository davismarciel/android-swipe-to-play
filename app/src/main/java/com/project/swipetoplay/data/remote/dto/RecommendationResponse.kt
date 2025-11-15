package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response model for recommendations from the API
 */
data class RecommendationResponse(
    @SerializedName("recommendations")
    val recommendations: List<GameResponse>,

    @SerializedName("count")
    val count: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("daily_limit_info")
    val dailyLimitInfo: DailyLimitInfoResponse? = null,

    @SerializedName("message")
    val message: String? = null
)

data class DailyLimitInfoResponse(
    @SerializedName("current_count")
    val currentCount: Int,

    @SerializedName("daily_limit")
    val dailyLimit: Int,

    @SerializedName("remaining_today")
    val remainingToday: Int,

    @SerializedName("limit_reached")
    val limitReached: Boolean
)

