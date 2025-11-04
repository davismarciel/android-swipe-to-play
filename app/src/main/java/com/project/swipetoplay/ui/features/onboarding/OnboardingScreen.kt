package com.project.swipetoplay.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.swipetoplay.data.repository.GameRepository
import com.project.swipetoplay.data.repository.UserPreferenceRepository
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.ProfileText
import com.project.swipetoplay.ui.theme.ProfileIconPurple

/**
 * Onboarding screen - multi-step wizard for initial user preferences
 * This screen is mandatory and cannot be skipped
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(
            gameRepository = com.project.swipetoplay.data.repository.GameRepository(),
            userPreferenceRepository = com.project.swipetoplay.data.repository.UserPreferenceRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        android.util.Log.d("OnboardingScreen", "🚀 Screen displayed")
        android.util.Log.d("OnboardingScreen", "⏳ Waiting 800ms for token to be saved...")
        kotlinx.coroutines.delay(800)
        
        val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
        if (tokenManager?.isAuthenticated() == true) {
            android.util.Log.d("OnboardingScreen", "✅ Token verified, loading initial data")
            viewModel.loadInitialData()
        } else {
            android.util.Log.w("OnboardingScreen", "⚠️ No token available yet, will retry...")
            kotlinx.coroutines.delay(500)
            val retryTokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
            if (retryTokenManager?.isAuthenticated() == true) {
                android.util.Log.d("OnboardingScreen", "✅ Token verified on retry, loading initial data")
                viewModel.loadInitialData()
            } else {
                android.util.Log.e("OnboardingScreen", "❌ Token still not available after retry")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {
        LinearProgressIndicator(
            progress = { (uiState.currentStep + 1).toFloat() / uiState.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = ProfileIconPurple,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (uiState.currentStep) {
                    0 -> "What genres do you like?"
                    1 -> "Which platforms do you use?"
                    2 -> "Monetization preferences"
                    else -> ""
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (uiState.currentStep) {
                    0 -> "Select at least one genre to help us recommend games you'll love"
                    1 -> "Choose the platforms you play games on"
                    2 -> "Tell us about your preferences for in-game purchases"
                    else -> ""
                },
                fontSize = 16.sp,
                color = ProfileText.copy(alpha = 0.7f)
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
                2 -> MonetizationPreferencesStep(
                    toleranceMicrotransactions = uiState.toleranceMicrotransactions,
                    toleranceDlc = uiState.toleranceDlc,
                    toleranceLootBoxes = uiState.toleranceLootBoxes,
                    toleranceBattlePass = uiState.toleranceBattlePass,
                    preferCosmeticOnly = uiState.preferCosmeticOnly,
                    avoidSubscription = uiState.avoidSubscription,
                    onUpdate = { key, value ->
                        viewModel.updateMonetizationPreference(key, value)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.currentStep > 0) {
                TextButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back",
                        color = ProfileText,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
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
                modifier = Modifier.weight(if (uiState.currentStep > 0) 1f else 0f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileIconPurple,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...", color = Color.White)
                } else {
                    Text(
                        text = if (uiState.currentStep < uiState.totalSteps - 1) "Next" else "Complete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun GenreSelectionStep(
    availableGenres: List<com.project.swipetoplay.data.remote.dto.GenreResponse>,
    selectedGenres: Map<Int, Int>,
    onToggleGenre: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select your favorite genres (at least one required)",
            fontSize = 14.sp,
            color = ProfileText.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                count = availableGenres.size,
                key = { index -> availableGenres[index].id }
            ) { index ->
                val genre = availableGenres[index]
                val isSelected = selectedGenres.containsKey(genre.id)
                val weight = selectedGenres[genre.id] ?: 5

                GenreChip(
                    genre = genre,
                    isSelected = isSelected,
                    weight = weight,
                    onToggle = { onToggleGenre(genre.id) },
                    onWeightChange = { newWeight ->
                        onToggleGenre(genre.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun GenreChip(
    genre: com.project.swipetoplay.data.remote.dto.GenreResponse,
    isSelected: Boolean,
    weight: Int,
    onToggle: () -> Unit,
    onWeightChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                ProfileIconPurple.copy(alpha = 0.3f)
            } else {
                Color.Gray.copy(alpha = 0.1f)
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, ProfileIconPurple)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = genre.name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = ProfileText
            )

            if (isSelected) {
                Text(
                    text = "Weight: $weight",
                    fontSize = 12.sp,
                    color = ProfileText.copy(alpha = 0.7f)
                )
            }
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
            isSelected = preferWindows,
            onToggle = { onPlatformToggle("windows", it) }
        )
        PlatformOption(
            platform = "Mac",
            isSelected = preferMac,
            onToggle = { onPlatformToggle("mac", it) }
        )
        PlatformOption(
            platform = "Linux",
            isSelected = preferLinux,
            onToggle = { onPlatformToggle("linux", it) }
        )
    }
}

@Composable
private fun PlatformOption(
    platform: String,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                ProfileIconPurple.copy(alpha = 0.3f)
            } else {
                Color.Gray.copy(alpha = 0.1f)
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, ProfileIconPurple)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = platform,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = ProfileText
            )
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = ProfileIconPurple
                )
            )
        }
    }
}

@Composable
private fun MonetizationPreferencesStep(
    toleranceMicrotransactions: Int,
    toleranceDlc: Int,
    toleranceLootBoxes: Int,
    toleranceBattlePass: Int,
    preferCosmeticOnly: Boolean,
    avoidSubscription: Boolean,
    onUpdate: (String, Any) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ToleranceSlider(
            label = "Microtransactions",
            value = toleranceMicrotransactions,
            onValueChange = { onUpdate("tolerance_microtransactions", it) }
        )
        ToleranceSlider(
            label = "DLC",
            value = toleranceDlc,
            onValueChange = { onUpdate("tolerance_dlc", it) }
        )
        ToleranceSlider(
            label = "Loot Boxes",
            value = toleranceLootBoxes,
            onValueChange = { onUpdate("tolerance_loot_boxes", it) }
        )
        ToleranceSlider(
            label = "Battle Pass",
            value = toleranceBattlePass,
            onValueChange = { onUpdate("tolerance_battle_pass", it) }
        )

        Divider()

        CheckboxOption(
            text = "Prefer cosmetic-only purchases",
            isChecked = preferCosmeticOnly,
            onCheckedChange = { onUpdate("prefer_cosmetic_only", it) }
        )
        CheckboxOption(
            text = "Avoid subscription-based games",
            isChecked = avoidSubscription,
            onCheckedChange = { onUpdate("avoid_subscription", it) }
        )
    }
}

@Composable
private fun ToleranceSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ProfileText
            )
            Text(
                text = "$value/10",
                fontSize = 14.sp,
                color = ProfileText.copy(alpha = 0.7f)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = ProfileIconPurple,
                activeTrackColor = ProfileIconPurple
            )
        )
    }
}

@Composable
private fun CheckboxOption(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ProfileIconPurple
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = ProfileText
        )
    }
}

