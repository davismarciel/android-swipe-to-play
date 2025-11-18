package com.project.swipetoplay.ui.features.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.repository.OnboardingRepository
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.ProfileText
import com.project.swipetoplay.ui.theme.ProfileIconPurple

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(
            gameRepository = com.project.swipetoplay.data.repository.GameRepository(),
            onboardingRepository = com.project.swipetoplay.data.repository.OnboardingRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        com.project.swipetoplay.data.error.ErrorLogger.logDebug("OnboardingScreen", "Screen displayed")
        com.project.swipetoplay.data.error.ErrorLogger.logDebug("OnboardingScreen", "Waiting 800ms for token to be saved")
        kotlinx.coroutines.delay(800)
        
        val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
        if (tokenManager?.isAuthenticated() == true) {
            com.project.swipetoplay.data.error.ErrorLogger.logDebug("OnboardingScreen", "Token verified, loading initial data")
            viewModel.loadInitialData()
        } else {
            com.project.swipetoplay.data.error.ErrorLogger.logWarning("OnboardingScreen", "No token available yet, will retry", null)
            kotlinx.coroutines.delay(500)
            val retryTokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
            if (retryTokenManager?.isAuthenticated() == true) {
                com.project.swipetoplay.data.error.ErrorLogger.logDebug("OnboardingScreen", "Token verified on retry, loading initial data")
                viewModel.loadInitialData()
            } else {
                com.project.swipetoplay.data.error.ErrorLogger.logError("OnboardingScreen", "Token still not available after retry", null)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {
        val progress = (uiState.currentStep + 1).toFloat() / uiState.totalSteps.toFloat()
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(300),
            label = "progress"
        )
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = ProfileIconPurple,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Step ${uiState.currentStep + 1} of ${uiState.totalSteps}",
                fontSize = 14.sp,
                color = ProfileText.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (uiState.currentStep) {
                    0 -> "Choose Your Favorite Genres"
                    1 -> "Select Your Platforms"
                    2 -> "Community Ratings Preferences"
                    else -> ""
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileText,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (uiState.currentStep) {
                    0 -> "Select the genres you enjoy. You can choose multiple options to help us personalize your recommendations."
                    1 -> "Choose the platforms where you play games. Select all that apply."
                    2 -> "Tell us your tolerance level for these community-rated aspects. This helps us filter games that match your preferences."
                    else -> ""
                },
                fontSize = 16.sp,
                color = ProfileText.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (uiState.currentStep) {
                0 -> GenreSelectionStep(
                    availableGenres = uiState.availableGenres,
                    selectedGenres = uiState.selectedGenres,
                    onToggleGenre = { genreId ->
                        viewModel.toggleGenre(genreId)
                    }
                )
                1 -> PlatformSelectionStep(
                    preferWindows = uiState.preferWindows,
                    preferMac = uiState.preferMac,
                    preferLinux = uiState.preferLinux,
                    onPlatformToggle = { platform, enabled ->
                        viewModel.updatePlatformPreference(platform, enabled)
                    }
                )
                2 -> CommunityRatingsStep(
                    toleranceToxicity = uiState.toleranceToxicity,
                    toleranceBugs = uiState.toleranceBugs,
                    toleranceMicrotransactions = uiState.toleranceMicrotransactions,
                    toleranceOptimization = uiState.toleranceOptimization,
                    toleranceCheaters = uiState.toleranceCheaters,
                    onUpdate = { key, value ->
                        viewModel.updateRatingTolerance(key, value)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            color = ProfileBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = if (uiState.currentStep > 0) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Button(
                    onClick = {
                        if (uiState.currentStep < uiState.totalSteps - 1) {
                            if (viewModel.canProceedToNext()) {
                                viewModel.nextStep()
                            }
                        } else {
                            viewModel.completeOnboarding(onComplete)
                        }
                    },
                    enabled = viewModel.canProceedToNext() && !uiState.isLoading,
                    modifier = if (uiState.currentStep > 0) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.wrapContentWidth()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileIconPurple,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                        disabledContentColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(
                            text = if (uiState.currentStep < uiState.totalSteps - 1) "Next" else "Complete",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@ExperimentalLayoutApi
@Composable
private fun GenreSelectionStep(
    availableGenres: List<com.project.swipetoplay.data.remote.dto.GenreResponse>,
    selectedGenres: Map<Int, Int>,
    onToggleGenre: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        availableGenres.forEach { genre ->
            val isSelected = selectedGenres.containsKey(genre.id)
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = tween(200),
                label = "scale"
            )

            GenreChip(
                genre = genre,
                isSelected = isSelected,
                onClick = { onToggleGenre(genre.id) },
                modifier = Modifier
                    .scale(scale)
            )
        }
    }
}

@Composable
private fun GenreChip(
    genre: com.project.swipetoplay.data.remote.dto.GenreResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) {
            ProfileIconPurple.copy(alpha = 0.2f)
        } else {
            Color.White.copy(alpha = 0.05f)
        },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, ProfileIconPurple)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = genre.name,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) ProfileIconPurple else ProfileText.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PlatformSelectionStep(
    preferWindows: Boolean,
    preferMac: Boolean,
    preferLinux: Boolean,
    onPlatformToggle: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PlatformOption(
            platform = "Windows",
            icon = Icons.Default.Computer,
            isSelected = preferWindows,
            onToggle = { onPlatformToggle("windows", it) }
        )
        PlatformOption(
            platform = "macOS",
            icon = Icons.Default.PhoneIphone,
            isSelected = preferMac,
            onToggle = { onPlatformToggle("mac", it) }
        )
        PlatformOption(
            platform = "Linux",
            icon = Icons.Default.Code,
            isSelected = preferLinux,
            onToggle = { onPlatformToggle("linux", it) }
        )
    }
}

@Composable
private fun PlatformOption(
    platform: String,
    icon: ImageVector,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle(!isSelected) },
        color = if (isSelected) {
            ProfileIconPurple.copy(alpha = 0.15f)
        } else {
            Color.White.copy(alpha = 0.05f)
        },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, ProfileIconPurple)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) ProfileIconPurple else ProfileText.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = platform,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) ProfileText else ProfileText.copy(alpha = 0.8f)
                )
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = ProfileIconPurple,
                    uncheckedColor = ProfileText.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun CommunityRatingsStep(
    toleranceToxicity: Int,
    toleranceBugs: Int,
    toleranceMicrotransactions: Int,
    toleranceOptimization: Int,
    toleranceCheaters: Int,
    onUpdate: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RatingSlider(
            label = "Toxicity",
            description = "Community toxicity level",
            icon = Icons.Default.Warning,
            value = toleranceToxicity,
            onValueChange = { onUpdate("toxicity", it) },
            color = Color(0xFFE53935)
        )
        
        RatingSlider(
            label = "Bugs",
            description = "Game bugs and glitches",
            icon = Icons.Default.BugReport,
            value = toleranceBugs,
            onValueChange = { onUpdate("bugs", it) },
            color = Color(0xFFFF9800)
        )
        
        RatingSlider(
            label = "Microtransactions",
            description = "In-game purchases",
            icon = Icons.Default.AttachMoney,
            value = toleranceMicrotransactions,
            onValueChange = { onUpdate("microtransactions", it) },
            color = Color(0xFFFFEB3B)
        )
        
        RatingSlider(
            label = "Poor Optimization",
            description = "Performance issues",
            icon = Icons.Default.Speed,
            value = toleranceOptimization,
            onValueChange = { onUpdate("optimization", it) },
            color = Color(0xFF4CAF50)
        )
        
        RatingSlider(
            label = "Cheaters",
            description = "Cheating in multiplayer",
            icon = Icons.Default.Shield,
            value = toleranceCheaters,
            onValueChange = { onUpdate("cheaters", it) },
            color = Color(0xFF9C27B0)
        )
    }
}

@Composable
private fun RatingSlider(
    label: String,
    description: String,
    icon: ImageVector,
    value: Int,
    onValueChange: (Int) -> Unit,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileText
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = ProfileText.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "$value/10",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Very Low",
                    fontSize = 11.sp,
                    color = ProfileText.copy(alpha = 0.5f)
                )
                Text(
                    text = "Very High",
                    fontSize = 11.sp,
                    color = ProfileText.copy(alpha = 0.5f)
                )
            }
        }
    }
}
