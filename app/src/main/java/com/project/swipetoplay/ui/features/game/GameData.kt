package com.project.swipetoplay.ui.features.game

import androidx.compose.ui.graphics.Color

data class Game(
    val id: String,
    val name: String,
    val description: String,
    val releaseDate: String,
    val tags: List<String>,
    val appId: String? = null,
    val imageUrl: String? = null,
    val imageResource: Int? = null
) {
    // Helper function to generate Steam CDN URL
    fun getSteamImageUrl(): String? {
        return appId?.let {
            "https://steamcdn-a.akamaihd.net/steam/apps/$it/library_600x900_2x.jpg"
        } ?: imageUrl
    }
}

data class GameTag(
    val text: String,
    val backgroundColor: Color = Color(0xFF585858)
)

// Mock data for the three example games
object MockGameData {
    val counterStrike2 = Game(
        id = "cs2",
        name = "Counter Strike 2",
        description = "Counter-Strike 2 is the largest technical leap forward in Counter-Strike's history, ensuring new features and updates..",
        releaseDate = "RELEASE DATE: 2023",
        tags = listOf("FPS", "COMPETITIVE", "MULTIPLAYER"),
        appId = "730"
    )

    val deadlock = Game(
        id = "deadlock",
        name = "Deadlock",
        description = "Deadlock is in early development with lots of temporary art and experimental gameplay. Access is currently limited to...",
        releaseDate = "RELEASE DATE: BETA",
        tags = listOf("MOBA", "COMPETITIVE", "MULTIPLAYER"),
        appId = "1422450"
    )

    val repo = Game(
        id = "repo",
        name = "R.E.P.O",
        description = "An online co-op horror game with up to 6 players. Locate valuable, fully physics-based objects and handle them with...",
        releaseDate = "RELEASE DATE: BETA",
        tags = listOf("TERROR", "MULTIPLAYER", "CO-OP"),
        appId = "3241660"
    )

    val valorant = Game(
        id = "valorant",
        name = "VALORANT",
        description = "A 5v5 character-based tactical FPS where precise gunplay meets unique agent abilities.",
        releaseDate = "RELEASE DATE: 2020",
        tags = listOf("FPS", "TACTICAL", "COMPETITIVE"),
        appId = "1274570" // VALORANT Steam App ID
    )

    val apexLegends = Game(
        id = "apex",
        name = "Apex Legends",
        description = "A free-to-play battle royale game where legendary characters with powerful abilities team up to fight for fame and fortune.",
        releaseDate = "RELEASE DATE: 2019",
        tags = listOf("BATTLE ROYALE", "FPS", "MULTIPLAYER"),
        appId = "1172470" // Apex Legends Steam App ID
    )

    val cyberpunk2077 = Game(
        id = "cyberpunk",
        name = "Cyberpunk 2077",
        description = "An open-world, action-adventure story set in Night City, a megalopolis obsessed with power, glamour and ceaseless body modification.",
        releaseDate = "RELEASE DATE: 2020",
        tags = listOf("RPG", "OPEN WORLD", "FUTURISTIC"),
        appId = "1091500" // Cyberpunk 2077 Steam App ID
    )

    val allGames = listOf(counterStrike2, deadlock, repo, valorant, apexLegends, cyberpunk2077)
}
