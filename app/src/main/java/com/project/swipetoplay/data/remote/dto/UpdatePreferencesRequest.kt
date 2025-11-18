package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class UpdatePreferencesRequest(
    @SerializedName("prefer_windows")
    val preferWindows: Boolean? = null,
    
    @SerializedName("prefer_mac")
    val preferMac: Boolean? = null,
    
    @SerializedName("prefer_linux")
    val preferLinux: Boolean? = null,
    
    @SerializedName("prefer_competitive")
    val preferCompetitive: Boolean? = null,
    
    @SerializedName("prefer_free_to_play")
    val preferFreeToPlay: Boolean? = null,
    
    @SerializedName("prefer_single_player")
    val preferSinglePlayer: Boolean? = null,
    
    @SerializedName("prefer_multiplayer")
    val preferMultiplayer: Boolean? = null,
    
    @SerializedName("prefer_coop")
    val preferCoop: Boolean? = null
)

