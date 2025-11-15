package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for completing onboarding
 */
data class OnboardingCompleteRequest(
    @SerializedName("preferences")
    val preferences: PreferencesData? = null,
    
    @SerializedName("monetization")
    val monetization: MonetizationData? = null,
    
    @SerializedName("genres")
    val genres: List<GenrePreferenceItem>? = null,
    
    @SerializedName("categories")
    val categories: List<CategoryPreferenceItem>? = null
)

/**
 * General preferences data
 */
data class PreferencesData(
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
    
    @SerializedName("include_early_access")
    val includeEarlyAccess: Boolean? = null
)

/**
 * Monetization preferences data
 */
data class MonetizationData(
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

/**
 * Onboarding status response
 */
data class OnboardingStatusResponse(
    @SerializedName("completed")
    val completed: Boolean,
    
    @SerializedName("has_preferences")
    val hasPreferences: Boolean,
    
    @SerializedName("has_monetization")
    val hasMonetization: Boolean,
    
    @SerializedName("genres_count")
    val genresCount: Int,
    
    @SerializedName("categories_count")
    val categoriesCount: Int
)

