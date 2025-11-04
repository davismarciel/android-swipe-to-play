package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for updating monetization preferences
 */
data class UpdateMonetizationPreferencesRequest(
    @SerializedName("prefer_one_time_purchase")
    val preferOneTimePurchase: Boolean? = null,
    
    @SerializedName("avoid_subscription")
    val avoidSubscription: Boolean? = null,
    
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
    val preferCosmeticOnly: Boolean? = null
)

