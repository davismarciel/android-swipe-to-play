package com.project.swipetoplay.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {}
) {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@gmail.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Edit Profile",
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

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ProfileText)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = ProfileIconPurple
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", color = ProfileText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfileIconPurple,
                    unfocusedBorderColor = ProfileText.copy(alpha = 0.5f),
                    focusedTextColor = ProfileText,
                    unfocusedTextColor = ProfileText,
                    focusedLabelColor = ProfileIconPurple,
                    unfocusedLabelColor = ProfileText.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field (Read-only from Google SSO)
            OutlinedTextField(
                value = email,
                onValueChange = { },
                label = { Text("Email", color = ProfileText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = ProfileText.copy(alpha = 0.3f),
                    disabledTextColor = ProfileText.copy(alpha = 0.5f),
                    disabledLabelColor = ProfileText.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Email cannot be changed (Google SSO)",
                color = ProfileText.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

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

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    SwipeToPlayTheme {
        EditProfileScreen()
    }
}
