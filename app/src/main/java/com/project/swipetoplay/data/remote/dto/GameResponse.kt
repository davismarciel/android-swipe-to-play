package com.project.swipetoplay.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response model for game data from the API
 */
data class GameResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("steam_id")
    val steamId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("slug")
    val slug: String? = null,

    @SerializedName("short_description")
    val shortDescription: String? = null,

    @SerializedName("required_age")
    val requiredAge: Int? = null,

    @SerializedName("is_free")
    val isFree: Boolean = false,

    @SerializedName("have_dlc")
    val haveDlc: Boolean = false,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("supported_languages")
    val supportedLanguages: List<String>? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("coming_soon")
    val comingSoon: Boolean = false,

    @SerializedName("recommendations")
    val recommendations: Int? = null,

    @SerializedName("achievements_count")
    val achievementsCount: Int? = null,

    @SerializedName("positive_reviews")
    val positiveReviews: Int? = null,

    @SerializedName("negative_reviews")
    val negativeReviews: Int? = null,

    @SerializedName("total_reviews")
    val totalReviews: Int? = null,

    @SerializedName("positive_ratio")
    val positiveRatio: Double? = null,

    @SerializedName("content_descriptors")
    val contentDescriptors: ContentDescriptorsResponse? = null,

    @SerializedName("genres")
    val genres: List<GenreResponse>? = null,

    @SerializedName("categories")
    val categories: List<CategoryResponse>? = null,

    @SerializedName("platform")
    val platform: PlatformResponse? = null,

    @SerializedName("developers")
    val developers: List<DeveloperResponse>? = null,

    @SerializedName("publishers")
    val publishers: List<PublisherResponse>? = null,

    @SerializedName("requirements")
    val requirements: RequirementsResponse? = null,

    @SerializedName("community_rating")
    val communityRating: CommunityRatingResponse? = null,

    @SerializedName("media")
    val media: List<MediaItemResponse>? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Get Steam URL for the game
     */
    fun getSteamUrl(): String {
        return "https://store.steampowered.com/app/$steamId/"
    }

    /**
     * Get Steam library image URL (vertical, larger image)
     */
    fun getSteamLibraryImageUrl(): String? {
        return steamId?.let { 
            "https://steamcdn-a.akamaihd.net/steam/apps/$it/library_600x900_2x.jpg"
        }
    }

    /**
     * Get Steam header image URL (horizontal, smaller icon)
     * This is the icon used in Steam store pages
     */
    fun getSteamHeaderImageUrl(): String? {
        return icon ?: steamId?.let {
            "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$it/header.jpg"
        }
    }
}

data class GenreResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class CategoryResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class PlatformResponse(
    @SerializedName("windows")
    val windows: Boolean = false,

    @SerializedName("mac")
    val mac: Boolean = false,

    @SerializedName("linux")
    val linux: Boolean = false
)

data class DeveloperResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class PublisherResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class RequirementsResponse(
    @SerializedName("pc_requirements")
    val pcRequirements: String? = null,

    @SerializedName("mac_requirements")
    val macRequirements: String? = null,

    @SerializedName("linux_requirements")
    val linuxRequirements: String? = null
)

data class CommunityRatingResponse(
    @SerializedName("toxicity")
    val toxicity: Double? = null,

    @SerializedName("bugs")
    val bugs: Double? = null,

    @SerializedName("microtransactions")
    val microtransactions: Double? = null,

    @SerializedName("optimization")
    val optimization: Double? = null,

    @SerializedName("cheaters")
    val cheaters: Double? = null
)

/**
 * Individual media item in the media array
 */
data class MediaItemResponse(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null
)

/**
 * Legacy MediaResponse - kept for backward compatibility if needed
 * Note: The API returns media as an array, so use List<MediaItemResponse>
 */
data class MediaResponse(
    @SerializedName("header_image")
    val headerImage: String? = null,

    @SerializedName("screenshots")
    val screenshots: List<String>? = null,

    @SerializedName("movies")
    val movies: List<MovieResponse>? = null
)

data class MovieResponse(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null,

    @SerializedName("webm")
    val webm: Map<String, String>? = null,

    @SerializedName("mp4")
    val mp4: Map<String, String>? = null
)

data class ContentDescriptorsResponse(
    @SerializedName("ids")
    val ids: List<Int>? = null,

    @SerializedName("notes")
    val notes: String? = null
)

