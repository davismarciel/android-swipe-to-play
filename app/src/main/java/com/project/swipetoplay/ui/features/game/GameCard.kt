package com.project.swipetoplay.ui.features.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onViewClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .width(387.dp)
            .height(603.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fallback background (always present)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2C2C2C))
                    .clip(RoundedCornerShape(30.dp))
            ) {
                Text(
                    text = game.name,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            // Game Image Background (overlays the fallback)
            val imageUrl = game.getSteamImageUrl()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = game.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Information Section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Game Name
                Text(
                    text = game.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    game.tags.take(3).forEach { tag ->
                        GameTagChip(tag = tag)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Release Date
                Text(
                    text = game.releaseDate,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = game.description,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // View Button - now inside the text container
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    FloatingActionButton(
                        onClick = onViewClick,
                        modifier = Modifier.size(40.dp),
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View Game",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color(0xFF585858) // Gray color from design
    ) {
        Text(
            text = tag,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardPreview() {
    SwipeToPlayTheme {
        GameCard(
            game = MockGameData.counterStrike2,
            onViewClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardDeadlockPreview() {
    SwipeToPlayTheme {
        GameCard(
            game = MockGameData.deadlock,
            onViewClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameCardRepoPreview() {
    SwipeToPlayTheme {
        GameCard(
            game = MockGameData.repo,
            onViewClick = {}
        )
    }
}
