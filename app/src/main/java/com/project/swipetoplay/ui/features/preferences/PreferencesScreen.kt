package com.project.swipetoplay.ui.features.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {}
) {
    // State for platforms
    var windowsSelected by remember { mutableStateOf(false) }
    var macSelected by remember { mutableStateOf(false) }
    var linuxSelected by remember { mutableStateOf(false) }

    // State for interests (pre-selected: RPG, Simulation, Co-op)
    var actionSelected by remember { mutableStateOf(false) }
    var adventureSelected by remember { mutableStateOf(false) }
    var rpgSelected by remember { mutableStateOf(true) }
    var strategySelected by remember { mutableStateOf(false) }
    var simulationSelected by remember { mutableStateOf(true) }
    var singlePlayerSelected by remember { mutableStateOf(false) }
    var multiplayerSelected by remember { mutableStateOf(false) }
    var coOpSelected by remember { mutableStateOf(true) }
    var mmoSelected by remember { mutableStateOf(false) }
    var vrSelected by remember { mutableStateOf(false) }

    // State for play style
    var casualSelected by remember { mutableStateOf(false) }
    var competitiveSelected by remember { mutableStateOf(false) }
    var storyDrivenSelected by remember { mutableStateOf(false) }

    // State for monetization
    var freeToPlaySelected by remember { mutableStateOf(false) }
    var paidSelected by remember { mutableStateOf(false) }
    var subscriptionSelected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Preferences",
                    color = ProfileText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ProfileBackground
            )
        )

        // Content with Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // PLATFORMS Section
            PreferencesSection(
                title = "PLATFORMS"
            ) {
                CheckboxItem(
                    text = "Windows",
                    isSelected = windowsSelected,
                    onToggle = { windowsSelected = !windowsSelected }
                )
                CheckboxItem(
                    text = "Mac",
                    isSelected = macSelected,
                    onToggle = { macSelected = !macSelected }
                )
                CheckboxItem(
                    text = "Linux",
                    isSelected = linuxSelected,
                    onToggle = { linuxSelected = !linuxSelected }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // INTERESTS Section
            PreferencesSection(
                title = "INTERESTS"
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InterestTag(
                        text = "Action",
                        isSelected = actionSelected,
                        onToggle = { actionSelected = !actionSelected }
                    )
                    InterestTag(
                        text = "Adventure",
                        isSelected = adventureSelected,
                        onToggle = { adventureSelected = !adventureSelected }
                    )
                    InterestTag(
                        text = "RPG",
                        isSelected = rpgSelected,
                        onToggle = { rpgSelected = !rpgSelected }
                    )
                    InterestTag(
                        text = "Strategy",
                        isSelected = strategySelected,
                        onToggle = { strategySelected = !strategySelected }
                    )
                    InterestTag(
                        text = "Simulation",
                        isSelected = simulationSelected,
                        onToggle = { simulationSelected = !simulationSelected }
                    )
                    InterestTag(
                        text = "Single-player",
                        isSelected = singlePlayerSelected,
                        onToggle = { singlePlayerSelected = !singlePlayerSelected }
                    )
                    InterestTag(
                        text = "Multiplayer",
                        isSelected = multiplayerSelected,
                        onToggle = { multiplayerSelected = !multiplayerSelected }
                    )
                    InterestTag(
                        text = "Co-op",
                        isSelected = coOpSelected,
                        onToggle = { coOpSelected = !coOpSelected }
                    )
                    InterestTag(
                        text = "MMO",
                        isSelected = mmoSelected,
                        onToggle = { mmoSelected = !mmoSelected }
                    )
                    InterestTag(
                        text = "VR",
                        isSelected = vrSelected,
                        onToggle = { vrSelected = !vrSelected }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PLAY STYLE Section
            PreferencesSection(
                title = "PLAY STYLE"
            ) {
                CheckboxItem(
                    text = "Casual",
                    isSelected = casualSelected,
                    onToggle = { casualSelected = !casualSelected }
                )
                CheckboxItem(
                    text = "Competitive",
                    isSelected = competitiveSelected,
                    onToggle = { competitiveSelected = !competitiveSelected }
                )
                CheckboxItem(
                    text = "Story-driven",
                    isSelected = storyDrivenSelected,
                    onToggle = { storyDrivenSelected = !storyDrivenSelected }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MONETIZATION Section
            PreferencesSection(
                title = "MONETIZATION"
            ) {
                CheckboxItem(
                    text = "Free to Play",
                    isSelected = freeToPlaySelected,
                    onToggle = { freeToPlaySelected = !freeToPlaySelected }
                )
                CheckboxItem(
                    text = "Paid",
                    isSelected = paidSelected,
                    onToggle = { paidSelected = !paidSelected }
                )
                CheckboxItem(
                    text = "Subscription",
                    isSelected = subscriptionSelected,
                    onToggle = { subscriptionSelected = !subscriptionSelected }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Changes Button
            Button(
                onClick = onSaveChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileIconPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Changes",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreferencesSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = ProfileText.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun CheckboxItem(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = ProfileIconPurple,
                uncheckedColor = ProfileText.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = ProfileText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InterestTag(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProfileIconPurple else ProfileCardBackground
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else ProfileText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
