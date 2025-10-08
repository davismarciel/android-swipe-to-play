package com.project.swipetoplay.ui.features.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme

@Composable
fun GameCardDemo(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = ProfileBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Cards Demo",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Safe LazyRow with error handling
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(650.dp), // Fixed height to prevent layout crashes
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(MockGameData.allGames) { game ->
                        GameCard(
                            game = game,
                            onViewClick = {
                                // Handle view click safely
                                try {
                                    // Future: Navigate to game details
                                } catch (e: Exception) {
                                    // Log error but don't crash
                                    e.printStackTrace()
                                }
                            }
                        )
                    }
                }
                // Fallback UI if LazyRow crashes
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to load game cards. Please try again.",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardDemoPreview() {
    SwipeToPlayTheme {
        GameCardDemo()
    }
}
