package com.project.swipetoplay.ui.features.game

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.project.swipetoplay.R
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.remote.dto.GameResponse
import com.project.swipetoplay.domain.mapper.GameMapper
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.ProfileText
import com.project.swipetoplay.ui.theme.ProfileIconPurple
import androidx.core.net.toUri
import java.util.Locale

/**
 * Detailed game screen with full information, ratings, media carousel, and Steam link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Int,
    onNavigateBack: () -> Unit = {},
    viewModel: GameDetailViewModel = viewModel(
        factory = GameDetailViewModelFactory(
            gameRepository = GameRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(gameId) {
        viewModel.loadGameDetails(gameId)
    }

    val errorMessage = uiState.error
    val gameData = uiState.game

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ProfileBackground,
                            Color(0xFF1A1A1A),
                            ProfileBackground
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                gameData != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GameDetailContent(
                            game = gameData,
                            context = context,
                            onNavigateBack = onNavigateBack
                        )

                        SteamButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, gameData.getSteamUrl().toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)

                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameDetailContent(
    game: GameResponse,
    context: Context,
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 96.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Game name above the icon
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.name,
                    color = ProfileText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (game.releaseDate != null) {
                    val formattedDate = GameMapper.formatReleaseDate(game.releaseDate, game.comingSoon)
                    Text(
                        text = formattedDate,
                        color = ProfileText.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // Game icon
            val imageUrl = game.getSteamHeaderImageUrl()
            val headerAspectRatio = 460f / 215f
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = game.name,
                    modifier = Modifier
                        .height(140.dp)
                        .width(140.dp * headerAspectRatio)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(140.dp)
                        .width(140.dp * headerAspectRatio)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2C))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (game.shortDescription != null) {
                Column {
                    Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = game.shortDescription,
                        fontSize = 14.sp,
                        color = ProfileText.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }

            // Simple review indicator after description
            if (game.positiveRatio != null) {
                ReviewIndicator(positiveRatio = game.positiveRatio)
            }

            val hasRatings = game.communityRating != null && (
                game.communityRating.toxicity != null ||
                game.communityRating.bugs != null ||
                game.communityRating.microtransactions != null ||
                game.communityRating.optimization != null ||
                game.communityRating.cheaters != null
            )
            if (hasRatings) {
                RatingsSection(rating = game.communityRating)
            }

            if (!game.genres.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Genres",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        game.genres.forEach { genre ->
                            GenreChip(text = genre.name)
                        }
                    }
                }
            }

            if (!game.categories.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Categories",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        game.categories.take(5).forEach { category ->
                            GenreChip(text = category.name)
                        }
                    }
                }
            }

            val screenshots = game.media?.filter { it.url != null }?.mapNotNull { it.url }?.takeIf { it.isNotEmpty() }
            
            if (!screenshots.isNullOrEmpty()) {
                MediaCarousel(
                    items = screenshots.map { MediaCarouselItem(it, "image") },
                    onImageClick = { imageUrl ->
                        viewImageFullscreen(context, imageUrl)
                    }
                )
            }

            if (game.platform != null) {
                PlatformSupportSection(platform = game.platform)
            }

            if (game.requirements != null && game.platform != null) {
                HardwareRequirementsSection(
                    requirements = game.requirements,
                    platform = game.platform
                )
            }

            if (!game.developers.isNullOrEmpty() || !game.publishers.isNullOrEmpty()) {
                Column {
                    if (!game.developers.isNullOrEmpty()) {
                        Text(
                            text = "Developer${if (game.developers.size > 1) "s" else ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProfileText,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        game.developers.forEach { developer ->
                            Text(
                                text = "• ${developer.name}",
                                fontSize = 14.sp,
                                color = ProfileText.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    if (!game.publishers.isNullOrEmpty()) {
                        Text(
                            text = "Publisher${if (game.publishers.size > 1) "s" else ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProfileText,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                        game.publishers.forEach { publisher ->
                            Text(
                                text = "• ${publisher.name}",
                                fontSize = 14.sp,
                                color = ProfileText.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Opens an image in fullscreen using an intent
 */
private fun viewImageFullscreen(context: Context, imageUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, imageUrl.toUri())
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, imageUrl.toUri())
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        } catch (_: Exception) {
        }
    }
}


@Composable
private fun RatingsSection(rating: com.project.swipetoplay.data.remote.dto.CommunityRatingResponse) {
    var showHelpDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2A2A2A).copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Community Ratings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "?",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            RatingItem(
                label = "Toxicity",
                value = rating.toxicity,
                color = Color(0xFFE53935),
                icon = Icons.Default.Warning
            )
            RatingItem(
                label = "Bugs",
                value = rating.bugs,
                color = Color(0xFFFF9800),
                icon = Icons.Default.BugReport
            )
            RatingItem(
                label = "Microtransactions",
                value = rating.microtransactions,
                color = Color(0xFFFFEB3B),
                icon = Icons.Default.AttachMoney
            )
            RatingItem(
                label = "Poor Optimization",
                value = rating.optimization,
                color = Color(0xFF4CAF50),
                icon = Icons.Default.Speed
            )
            rating.cheaters?.let {
                RatingItem(
                    label = "Cheaters",
                    value = it,
                    color = Color(0xFF9C27B0),
                    icon = Icons.Default.Shield
                )
            }
        }
    }
    
    // Show help dialog
    if (showHelpDialog) {
        RatingHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

/**
 * Converts percentage to a rating score (0-5 scale)
 * Linear conversion: 0% = 1.0, 100% = 5.0
 */
private fun percentageToRating(percentage: Double): Double {
    return 1.0 + (percentage / 100.0) * 4.0
}

@Composable
private fun RatingItem(
    label: String,
    value: Double?,
    color: Color,
    isReversed: Boolean = false,
    icon: ImageVector? = null
) {
    if (value != null) {
        val percentage = (value * 100.0).coerceIn(0.0, 100.0)
        val displayPercentage = if (isReversed) (100.0 - percentage) else percentage
        val progressValue = (displayPercentage / 100.0).coerceIn(0.0, 1.0)
        val rating = percentageToRating(displayPercentage)
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", rating),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = "/5",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressValue.toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * Help dialog explaining how rating scores are calculated
 */
@Composable
private fun RatingHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 480.dp),
        containerColor = Color(0xFF1A1A2E),
        tonalElevation = 12.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ProfileIconPurple.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ProfileIconPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(
                    text = "Understanding Ratings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .widthIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "How to read:",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "1.0 - 2.5",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Fewer problems",
                                    color = ProfileText.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "2.5 - 3.5",
                                    color = Color(0xFFFF9800),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Moderate problems",
                                    color = ProfileText.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE53935).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "3.5 - 5.0",
                                    color = Color(0xFFE53935),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "More problems",
                                    color = ProfileText.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.List,
                            contentDescription = null,
                            tint = ProfileIconPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Metrics:",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricInfoItem("Toxicity", "Community toxicity level", Color(0xFFE53935), Icons.Default.Warning)
                        MetricInfoItem("Bugs", "Technical issues reported", Color(0xFFFF9800), Icons.Default.BugReport)
                        MetricInfoItem("Microtransactions", "In-game purchase presence", Color(0xFFFFEB3B), Icons.Default.AttachMoney)
                        MetricInfoItem("Poor Optimization", "Performance problems", Color(0xFF4CAF50), Icons.Default.Speed)
                        MetricInfoItem("Cheaters", "Cheater and hacker presence", Color(0xFF9C27B0), Icons.Default.Shield)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileIconPurple
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Got it",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    )
}

@Composable
private fun MetricInfoItem(
    name: String,
    description: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = ProfileText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun GenreChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF585858)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

data class MediaCarouselItem(
    val url: String,
    val type: String
)

@Composable
private fun MediaCarousel(
    items: List<MediaCarouselItem>,
    onImageClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Screenshots",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileText,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow (
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.size) { index ->
                AsyncImage(
                    model = items[index].url,
                    contentDescription = "Screenshots ${index + 1}",
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onImageClick(items[index].url)
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
private fun PlatformSupportSection(
    platform: com.project.swipetoplay.data.remote.dto.PlatformResponse
) {
    Column {
        Text(
            text = "Platform Support",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileText,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (platform.windows) PlatformBadge("Windows")
            if (platform.mac) PlatformBadge("Mac")
            if (platform.linux) PlatformBadge("Linux")
        }
    }
}

@Composable
private fun PlatformBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2196F3)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SteamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        containerColor = Color(0xFF1b2838),
        contentColor = Color(0xFF66c0f4)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_steam_logo),
            contentDescription = "View on Steam",
            modifier = Modifier.size(28.dp),
            colorFilter = ColorFilter.tint(Color(0xFF66c0f4))
        )
    }
}

@Composable
private fun ReviewIndicator(positiveRatio: Double) {
    val isPositive = positiveRatio > 0.5
    val percentage = (positiveRatio * 100).toInt()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isPositive) 
            Color(0xFF4CAF50).copy(alpha = 0.15f) 
        else 
            Color(0xFFE53935).copy(alpha = 0.15f),
        border = BorderStroke(
            1.dp, 
            if (isPositive) 
                Color(0xFF4CAF50).copy(alpha = 0.4f)
            else 
                Color(0xFFE53935).copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPositive) "😊" else "😞",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (isPositive) "Mostly Positive" else "Mostly Negative",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
                Text(
                    text = "$percentage% positive reviews",
                    fontSize = 14.sp,
                    color = ProfileText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun HardwareRequirementsSection(
    requirements: com.project.swipetoplay.data.remote.dto.RequirementsResponse,
    platform: com.project.swipetoplay.data.remote.dto.PlatformResponse
) {
    var expandedPlatform by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            text = "Hardware Requirements",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (platform.windows && !requirements.pcRequirements.isNullOrBlank()) {
                HardwareRequirementsCard(
                    platformName = "Windows",
                    requirementsText = requirements.pcRequirements,
                    isExpanded = expandedPlatform == "Windows",
                    onToggle = {
                        expandedPlatform = if (expandedPlatform == "Windows") null else "Windows"
                    }
                )
            }

            if (platform.mac && !requirements.macRequirements.isNullOrBlank()) {
                HardwareRequirementsCard(
                    platformName = "Mac",
                    requirementsText = requirements.macRequirements,
                    isExpanded = expandedPlatform == "Mac",
                    onToggle = {
                        expandedPlatform = if (expandedPlatform == "Mac") null else "Mac"
                    }
                )
            }

            if (platform.linux && !requirements.linuxRequirements.isNullOrBlank()) {
                HardwareRequirementsCard(
                    platformName = "Linux",
                    requirementsText = requirements.linuxRequirements,
                    isExpanded = expandedPlatform == "Linux",
                    onToggle = {
                        expandedPlatform = if (expandedPlatform == "Linux") null else "Linux"
                    }
                )
            }
        }
    }
}

@Composable
private fun HardwareRequirementsCard(
    platformName: String,
    requirementsText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A2A2A).copy(alpha = 0.6f),
        tonalElevation = 2.dp,
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = platformName,
                    color = ProfileText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = ProfileText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = formatRequirementsText(requirementsText),
                    color = ProfileText.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                
                // Show a preview of requirements
                val previewText = requirementsText
                    .take(80)
                    .replace(Regex("Minimum:|Recommended:"), "")
                    .trim()
                
                if (previewText.isNotBlank()) {
                    Text(
                        text = "$previewText...",
                        color = ProfileText.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatRequirementsText(text: String): String {
    // Clean up the text by removing HTML entities and formatting
    return text
        .replace(Regex("&amp;"), "&")
        .replace(Regex("&lt;"), "<")
        .replace(Regex("&gt;"), ">")
        .replace(Regex("Minimum:"), "\nMinimum:")
        .replace(Regex("Recommended:"), "\n\nRecommended:")
        .replace(Regex("OS:"), "\n• OS:")
        .replace(Regex("Processor:"), "\n• Processor:")
        .replace(Regex("Memory:"), "\n• Memory:")
        .replace(Regex("Graphics:"), "\n• Graphics:")
        .replace(Regex("Storage:"), "\n• Storage:")
        .replace(Regex("DirectX:"), "\n• DirectX:")
        .replace(Regex("Sound Card:"), "\n• Sound Card:")
        .replace(Regex("Additional Notes:"), "\n• Additional Notes:")
        .trim()
}

