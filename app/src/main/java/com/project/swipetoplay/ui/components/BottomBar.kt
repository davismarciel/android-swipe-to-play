package com.project.swipetoplay.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.project.swipetoplay.ui.theme.ProfileBottomBar

@Composable
fun BottomBar(
    currentScreen: String,
    onProfileClick: () -> Unit,
    onSwipeClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    NavigationBar(
        containerColor = ProfileBottomBar
    ) {
        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = onSwipeClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Swipe"
                )
            },
            label = { Text("Swipe") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentScreen == "details",
            onClick = onDetailsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details"
                )
            },
            label = { Text("Details") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
    }
}
