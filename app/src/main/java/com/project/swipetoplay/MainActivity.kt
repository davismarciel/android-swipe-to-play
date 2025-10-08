package com.project.swipetoplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.project.swipetoplay.ui.components.BottomBar
import com.project.swipetoplay.ui.components.LogoutDialog
import com.project.swipetoplay.ui.features.profile.ProfileScreen
import com.project.swipetoplay.ui.features.profile.EditProfileScreen
import com.project.swipetoplay.ui.features.preferences.PreferencesScreen
import com.project.swipetoplay.ui.features.notifications.NotificationScreen
import com.project.swipetoplay.ui.features.game.GameCardDemo
import com.project.swipetoplay.ui.features.home.HomeScreen
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        setContent {
            SwipeToPlayTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf("home") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    onNavigateToProfile = { currentScreen = "profile" },
                    onNavigateToDetails = { currentScreen = "gameDemo" }
                )
                "profile" -> ProfileScreen(
                    onNavigateToSettings = { currentScreen = "preferences" },
                    onNavigateToEditProfile = { currentScreen = "editProfile" },
                    onNavigateToNotifications = { currentScreen = "notifications" },
                    onLogout = { showLogoutDialog = true }
                )
                "preferences" -> PreferencesScreen(
                    onNavigateBack = { currentScreen = "profile" },
                    onSaveChanges = { currentScreen = "profile" }
                )
                "editProfile" -> EditProfileScreen(
                    onNavigateBack = { currentScreen = "profile" },
                    onSaveChanges = { currentScreen = "profile" }
                )
                "notifications" -> NotificationScreen(
                    onNavigateBack = { currentScreen = "profile" },
                    onSaveChanges = { currentScreen = "profile" }
                )
                "gameDemo" -> GameCardDemo()
            }
        }

        // Logout Dialog
        LogoutDialog(
            isVisible = showLogoutDialog,
            onConfirm = {
                showLogoutDialog = false
                currentScreen = "home"
            },
            onDismiss = { showLogoutDialog = false }
        )

        BottomBar(
            currentScreen = currentScreen,
            onProfileClick = { currentScreen = "profile" },
            onSwipeClick = { currentScreen = "home" },
            onDetailsClick = { currentScreen = "gameDemo" }
        )
    }
}