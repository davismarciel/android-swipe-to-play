package com.project.swipetoplay.ui.features.notifications

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.data.preferences.NotificationPreferences
import com.project.swipetoplay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {}
) {
    val context = LocalContext.current
    val notificationPrefs = remember { NotificationPreferences(context) }
    
    var swipeReadyNotification by remember { 
        mutableStateOf(notificationPrefs.swipeReadyNotification) 
    }
    var soundEnabled by remember { 
        mutableStateOf(notificationPrefs.soundEnabled) 
    }
    var vibrationEnabled by remember { 
        mutableStateOf(notificationPrefs.vibrationEnabled) 
    }
    var pushNotifications by remember { 
        mutableStateOf(notificationPrefs.pushNotifications) 
    }
    var emailNotifications by remember { 
        mutableStateOf(notificationPrefs.emailNotifications) 
    }
    
    fun saveSettings() {
        notificationPrefs.saveAll(
            swipeReady = swipeReadyNotification,
            sound = soundEnabled,
            vibration = vibrationEnabled,
            push = pushNotifications,
            email = emailNotifications
        )
        onSaveChanges()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
            TopAppBar(
            title = {
                Text(
                    text = "Notifications",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            NotificationSection(
                title = "Swipe Ready",
                description = "Get notified when you can swipe again"
            ) {
                SwitchItem(
                    text = "Enable swipe ready notifications",
                    isEnabled = swipeReadyNotification,
                    onToggle = { swipeReadyNotification = !swipeReadyNotification }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSection(
                title = "Sound & Vibration",
                description = "Control audio and haptic feedback"
            ) {
                SwitchItem(
                    text = "Sound effects",
                    isEnabled = soundEnabled,
                    onToggle = { soundEnabled = !soundEnabled }
                )
                SwitchItem(
                    text = "Vibration",
                    isEnabled = vibrationEnabled,
                    onToggle = { vibrationEnabled = !vibrationEnabled }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSection(
                title = "Notification Types",
                description = "Choose how you want to receive notifications"
            ) {
                SwitchItem(
                    text = "Push notifications",
                    isEnabled = pushNotifications,
                    onToggle = { pushNotifications = !pushNotifications }
                )
                SwitchItem(
                    text = "Email notifications",
                    isEnabled = emailNotifications,
                    onToggle = { emailNotifications = !emailNotifications }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileIconPurple
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Save Changes",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun NotificationSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = ProfileText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            color = ProfileText.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
private fun SwitchItem(
    text: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = ProfileText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ProfileIconPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ProfileText.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    SwipeToPlayTheme {
        NotificationScreen()
    }
}
