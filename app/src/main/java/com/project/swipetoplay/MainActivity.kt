package com.project.swipetoplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.swipetoplay.ui.features.login.LoginScreen
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwipeToPlayTheme {
                    LoginScreen()
            }
        }
    }
}