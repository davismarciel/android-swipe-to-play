package com.project.swipetoplay.ui.features.preferences

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.project.swipetoplay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {},
    viewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var shouldNavigateAfterSave by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Preferences saved successfully!",
                duration = SnackbarDuration.Short
            )
            if (shouldNavigateAfterSave) {
                kotlinx.coroutines.delay(1500)
                shouldNavigateAfterSave = false
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    fun handleBack() {
        if (uiState.hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }
    
    BackHandler(enabled = true) {
        handleBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Preferences",
                    color = ProfileText,
                        fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { handleBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileText
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.savePreferences() },
                    enabled = !uiState.isSaving && uiState.hasUnsavedChanges
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ProfileIconPurple,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Preferences",
                            tint = if (uiState.hasUnsavedChanges) ProfileIconPurple else ProfileText.copy(alpha = 0.4f)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
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
        if (uiState.isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                    CircularProgressIndicator(
                        color = ProfileIconPurple,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                        .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    ModernPreferencesCard(
                        icon = Icons.Default.Computer,
                        title = "Platforms",
                        subtitle = "Select your preferred gaming platforms"
                    ) {
                        PlatformRow(
                            platforms = listOf(
                                PlatformOption("Windows", uiState.windowsSelected, Icons.Default.Computer),
                                PlatformOption("Mac", uiState.macSelected, Icons.Default.PhoneIphone),
                                PlatformOption("Linux", uiState.linuxSelected, Icons.Default.Terminal)
                            ),
                            onToggle = { platform ->
                                viewModel.togglePlatform(platform)
                            }
                    )
                }

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernPreferencesCard(
                        icon = Icons.Default.Favorite,
                        title = "Genres",
                        subtitle = "Select your favorite game genres"
                ) {
                    // Organize genres in a well-structured grid with better spacing
                    if (uiState.availableGenres.isEmpty()) {
                        // Show message when no genres are available
                        Text(
                            text = if (uiState.error != null) {
                                "Unable to load genres. Tap to retry."
                            } else {
                                "No genres available"
                            },
                            color = ProfileText.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clickable(enabled = uiState.error != null) {
                                    viewModel.retry()
                                }
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Render genres dynamically from backend
                            uiState.availableGenres.forEach { genre ->
                                InterestTag(
                                    text = genre.name,
                                    isSelected = viewModel.isGenreSelected(genre.name),
                                    onToggle = { viewModel.toggleGenre(genre.name) }
                                )
                            }
                        }
                    }
                }

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernPreferencesCard(
                        icon = Icons.Default.SportsEsports,
                        title = "Play Style",
                        subtitle = "How do you like to play?"
                ) {
                        SwitchItem(
                        text = "Casual",
                            icon = Icons.Default.Games,
                        isSelected = uiState.casualSelected,
                        onToggle = { viewModel.togglePlayStyle("Casual") }
                    )
                        SwitchItem(
                        text = "Competitive",
                            icon = Icons.Default.EmojiEvents,
                        isSelected = uiState.competitiveSelected,
                        onToggle = { viewModel.togglePlayStyle("Competitive") }
                    )
                        SwitchItem(
                        text = "Story-driven",
                            icon = Icons.Default.Book,
                        isSelected = uiState.storyDrivenSelected,
                        onToggle = { viewModel.togglePlayStyle("Story-driven") }
                    )
                }

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernPreferencesCard(
                        icon = Icons.Default.AttachMoney,
                        title = "Monetization",
                        subtitle = "Payment preferences"
                ) {
                        SwitchItem(
                        text = "Free to Play",
                            icon = Icons.Default.CheckCircle,
                        isSelected = uiState.freeToPlaySelected,
                        onToggle = { viewModel.toggleMonetization("Free to Play") }
                    )
                        SwitchItem(
                            text = "Paid Games",
                            icon = Icons.Default.ShoppingCart,
                        isSelected = uiState.paidSelected,
                        onToggle = { viewModel.toggleMonetization("Paid") }
                    )
                        SwitchItem(
                        text = "Subscription",
                            icon = Icons.Default.Payment,
                        isSelected = uiState.subscriptionSelected,
                        onToggle = { viewModel.toggleMonetization("Subscription") }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = {
                Text(
                    text = "Unsaved Changes",
                    color = ProfileText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Do you want to save before leaving?",
                    color = ProfileText.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.savePreferences()
                        showUnsavedChangesDialog = false
                        shouldNavigateAfterSave = true
                    }
                ) {
                    Text(
                        text = "Save",
                        color = ProfileIconPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = "Discard",
                        color = ProfileText.copy(alpha = 0.6f)
                    )
                }
            },
            containerColor = Color(0xFF1E1E2E)
        )
    }
    }
}

data class PlatformOption(
    val name: String,
    val isSelected: Boolean,
    val icon: ImageVector
)

@Composable
private fun ModernPreferencesCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileIconPurple,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
                        color = ProfileText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = ProfileText.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            content()
        }
    }
}

@Composable
private fun PlatformRow(
    platforms: List<PlatformOption>,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        platforms.forEach { platform ->
            PlatformChip(
                platform = platform,
                onToggle = { onToggle(platform.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlatformChip(
    platform: PlatformOption,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (platform.isSelected) 1.05f else 1f,
        animationSpec = tween(300), label = ""
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (platform.isSelected) {
                ProfileIconPurple.copy(alpha = 0.3f)
            } else {
                Color(0xFF2A2A3A)
            }
        ),
        border = BorderStroke(
            width = if (platform.isSelected) 2.dp else 1.dp,
            color = if (platform.isSelected) ProfileIconPurple else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = platform.name,
                tint = if (platform.isSelected) ProfileIconPurple else ProfileText.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
        )
            Text(
                text = platform.name,
                color = if (platform.isSelected) ProfileText else ProfileText.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = if (platform.isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SwitchItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileIconPurple.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
        Text(
            text = text,
            color = ProfileText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
            }
            Switch(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ProfileIconPurple,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                    uncheckedTrackColor = Color(0xFF2A2A3A)
                )
            )
        }
    }
}

@Composable
private fun InterestTag(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(200), label = ""
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                ProfileIconPurple
            } else {
                Color(0xFF2A2A3A)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else ProfileText.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreferencesScreenPreview() {
    SwipeToPlayTheme {
        PreferencesScreen()
    }
}
