package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName


data class UpdatePreferredGenresRequest(
    @SerializedName("genres")
    val genres: List<GenrePreferenceItem>
)

data class GenrePreferenceItem(
    @SerializedName("genre_id")
    val genreId: Int,
    
    @SerializedName("preference_weight")
    val preferenceWeight: Int
)

