package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response model for user preferences from the API
 */
data class UserPreferenceResponse(
    @SerializedName("preferences")
    val preferences: UserPreferenceData? = null,

    @SerializedName("monetization_preferences")
    val monetizationPreferences: UserMonetizationPreferenceData? = null,

    @SerializedName("preferred_genres")
    val preferredGenres: List<GenreWithWeight>? = null,

    @SerializedName("preferred_categories")
    val preferredCategories: List<CategoryWithWeight>? = null,

    @SerializedName("profile")
    val profile: UserProfileData? = null
)

data class UserPreferenceData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("prefer_windows")
    val preferWindows: Boolean? = null,

    @SerializedName("prefer_mac")
    val preferMac: Boolean? = null,

    @SerializedName("prefer_linux")
    val preferLinux: Boolean? = null,

    @SerializedName("preferred_languages")
    val preferredLanguages: List<String>? = null,

    @SerializedName("prefer_single_player")
    val preferSinglePlayer: Boolean? = null,

    @SerializedName("prefer_multiplayer")
    val preferMultiplayer: Boolean? = null,

    @SerializedName("prefer_coop")
    val preferCoop: Boolean? = null,

    @SerializedName("prefer_competitive")
    val preferCompetitive: Boolean? = null,

    @SerializedName("min_age_rating")
    val minAgeRating: Int? = null,

    @SerializedName("avoid_violence")
    val avoidViolence: Boolean? = null,

    @SerializedName("avoid_nudity")
    val avoidNudity: Boolean? = null,

    @SerializedName("max_price")
    val maxPrice: Double? = null,

    @SerializedName("prefer_free_to_play")
    val preferFreeToPlay: Boolean? = null,

    @SerializedName("include_early_access")
    val includeEarlyAccess: Boolean? = null
)

data class UserMonetizationPreferenceData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("tolerance_microtransactions")
    val toleranceMicrotransactions: Int? = null,

    @SerializedName("tolerance_dlc")
    val toleranceDlc: Int? = null,

    @SerializedName("tolerance_season_pass")
    val toleranceSeasonPass: Int? = null,

    @SerializedName("tolerance_loot_boxes")
    val toleranceLootBoxes: Int? = null,

    @SerializedName("tolerance_battle_pass")
    val toleranceBattlePass: Int? = null,

    @SerializedName("tolerance_ads")
    val toleranceAds: Int? = null,

    @SerializedName("tolerance_pay_to_win")
    val tolerancePayToWin: Int? = null,

    @SerializedName("prefer_cosmetic_only")
    val preferCosmeticOnly: Boolean? = null,

    @SerializedName("avoid_subscription")
    val avoidSubscription: Boolean? = null,

    @SerializedName("prefer_one_time_purchase")
    val preferOneTimePurchase: Boolean? = null
)

data class GenreWithWeight(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("pivot")
    val pivot: GenrePivot? = null
)

data class GenrePivot(
    @SerializedName("preference_weight")
    val preferenceWeight: Int = 5
)

data class CategoryWithWeight(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("pivot")
    val pivot: CategoryPivot? = null
)

data class CategoryPivot(
    @SerializedName("preference_weight")
    val preferenceWeight: Int = 5
)

data class UserProfileData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("website")
    val website: String? = null
)

