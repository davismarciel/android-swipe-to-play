package com.project.swipetoplay.domain.mapper

import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.ui.features.game.Game
import com.project.swipetoplay.ui.features.game.PlatformInfo
import com.project.swipetoplay.ui.features.game.RequirementsInfo


object GameMapper {
    fun toGame(gameResponse: GameResponse): Game {
        val tags = mutableListOf<String>()
        gameResponse.genres?.forEach { genre -> tags.add(genre.name.uppercase()) }
        gameResponse.categories?.take(3)?.forEach { category -> 
            if (!tags.contains(category.name.uppercase())) {
                tags.add(category.name.uppercase())
            }
        }

        val releaseDate = formatReleaseDate(gameResponse.releaseDate, gameResponse.comingSoon)

        val platform = gameResponse.platform?.let {
            PlatformInfo(
                windows = it.windows,
                mac = it.mac,
                linux = it.linux
            )
        }

        val requirements = gameResponse.requirements?.let {
            RequirementsInfo(
                pcRequirements = it.pcRequirements,
                macRequirements = it.macRequirements,
                linuxRequirements = it.linuxRequirements
            )
        }

        return Game(
            id = gameResponse.id.toString(),
            name = gameResponse.name,
            description = gameResponse.shortDescription ?: "No description available",
            releaseDate = releaseDate,
            tags = tags,
            appId = gameResponse.steamId,
            imageUrl = gameResponse.icon ?: gameResponse.getSteamLibraryImageUrl(),
            platform = platform,
            requirements = requirements
        )
    }

    fun toGameList(gameResponses: List<GameResponse>): List<Game> {
        return gameResponses.map { toGame(it) }
    }

    
    fun formatReleaseDate(dateString: String?, comingSoon: Boolean): String {
        if (comingSoon) {
            return "COMING SOON"
        }

        if (dateString.isNullOrBlank()) {
            return "DATE NOT AVAILABLE"
        }

        return try {
            val cleanDate = dateString.trim()
            
            if (cleanDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = cleanDate.split("-")
                val year = parts[0]
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                
                val monthName = getMonthName(month)
                "Released on $monthName $day, $year"
            }
            else if (cleanDate.matches(Regex("\\d{4}-\\d{2}"))) {
                val parts = cleanDate.split("-")
                val year = parts[0]
                val month = parts[1].toInt()
                
                val monthName = getMonthName(month)
                "Released in $monthName $year"
            }
            else if (cleanDate.matches(Regex("\\d{4}"))) {
                "Released in $cleanDate"
            }
            else {
                val yearMatch = Regex("\\d{4}").find(cleanDate)
                if (yearMatch != null) {
                    "Released in ${yearMatch.value}"
                } else {
                    "DATE NOT AVAILABLE"
                }
            }
        } catch (e: Exception) {
            val yearMatch = Regex("\\d{4}").find(dateString)
            if (yearMatch != null) {
                "Released in ${yearMatch.value}"
            } else {
                "DATE NOT AVAILABLE"
            }
        }
    }

    
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Month $month"
        }
    }
}

