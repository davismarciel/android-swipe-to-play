package com.project.swipetoplay.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.theme.ProfileBottomBar

@Composable
fun BottomBar(
    currentScreen: String,
    onProfileClick: () -> Unit,
    onSwipeClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(89.dp),
        color = ProfileBottomBar
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = if (currentScreen == "profile") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Profile",
                    color = if (currentScreen == "profile") Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == "profile") FontWeight.Bold else FontWeight.Normal
                )
            }

            // Swipe (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onSwipeClick) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Swipe",
                        tint = if (currentScreen == "home") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Swipe",
                    color = if (currentScreen == "home") Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == "home") FontWeight.Bold else FontWeight.Normal
                )
            }

            // Details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onDetailsClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = if (currentScreen == "gameDemo") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Details",
                    color = if (currentScreen == "gameDemo") Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == "gameDemo") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
