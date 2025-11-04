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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.project.swipetoplay.domain.model.GoogleUser
import com.project.swipetoplay.ui.theme.*
import com.project.swipetoplay.ui.components.TopBar

@Composable
fun ProfileScreen(
    user: GoogleUser? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(ProfileText)
                        ) {
                            if (user?.profilePictureUrl != null) {
                                AsyncImage(
                                    model = user.profilePictureUrl,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center),
                                    tint = ProfileIconPurple
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user?.displayName ?: "User",
                    color = ProfileText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    letterSpacing = 0.5.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = user?.email ?: "",
                    color = ProfileText.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

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

                ProfileSection(
                    title = "Account",
                    items = listOf(
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
            .height(80.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = item.iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(22.dp),
                    tint = item.iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                color = ProfileText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = ProfileText.copy(alpha = 0.6f)
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
            onNavigateToNotifications = {},
            onLogout = {}
        )
    }
}
