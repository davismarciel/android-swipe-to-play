package com.project.swipetoplay.ui.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.theme.*
import com.project.swipetoplay.ui.components.TopBar

@Composable
fun DetailsScreen() {
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = ProfileBackground.copy(alpha = 0.3f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Coming soon",
                            modifier = Modifier.size(64.dp),
                            tint = ProfileText.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Something new is coming",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We are working on amazing new features for you. Soon, this section will have exclusive functionalities and special content.",
                    fontSize = 16.sp,
                    color = ProfileText.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    repeat(3) { index ->
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = RoundedCornerShape(50),
                            color = ProfileText.copy(alpha = 0.3f + (index * 0.2f))
                        ) {}
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    SwipeToPlayTheme {
        DetailsScreen()
    }
}

