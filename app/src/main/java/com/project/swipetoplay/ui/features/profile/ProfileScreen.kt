package com.project.swipetoplay.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Swipe To Play",
                    color = ProfileText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { /* Search action */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ProfileText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ProfileBackground
            )
        )

        // Profile Content with Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ProfileText)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center),
                    tint = ProfileIconPurple
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Section
            ProfileSection(
                title = "Settings",
                items = listOf(
                    ProfileMenuItem(
                        title = "Manage preferences",
                        icon = Icons.Default.Settings,
                        iconColor = ProfileText,
                        onClick = onNavigateToSettings
                    ),
                    ProfileMenuItem(
                        title = "Notifications",
                        icon = Icons.Default.Notifications,
                        iconColor = ProfileText,
                        onClick = onNavigateToNotifications
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account Section
            ProfileSection(
                title = "Account",
                items = listOf(
                    ProfileMenuItem(
                        title = "Edit Profile",
                        icon = Icons.Default.Edit,
                        iconColor = ProfileText,
                        onClick = onNavigateToEditProfile
                    ),
                    ProfileMenuItem(
                        title = "Log out",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconColor = ProfileIconRed,
                        onClick = onLogout
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column {
        Text(
            text = title,
            color = ProfileText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEach { item ->
            ProfileMenuItemCard(
                item = item
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileMenuItemCard(
    item: ProfileMenuItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(25.dp),
                tint = item.iconColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                color = ProfileText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = ProfileText
            )
        }
    }
}


data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SwipeToPlayTheme {
        ProfileScreen(
            onNavigateToSettings = {},
            onNavigateToEditProfile = {},
            onNavigateToNotifications = {},
            onLogout = {}
        )
    }
}
