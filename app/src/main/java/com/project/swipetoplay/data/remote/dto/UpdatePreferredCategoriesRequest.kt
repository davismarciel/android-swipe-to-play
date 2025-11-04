package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for updating preferred categories
 */
data class UpdatePreferredCategoriesRequest(
    @SerializedName("categories")
    val categories: List<CategoryPreferenceItem>
)

data class CategoryPreferenceItem(
    @SerializedName("category_id")
    val categoryId: Int,
    
    @SerializedName("preference_weight")
    val preferenceWeight: Int
)

