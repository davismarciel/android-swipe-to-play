package com.project.swipetoplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.project.swipetoplay.data.auth.GoogleAuthManager
import com.project.swipetoplay.ui.components.BottomBar
import com.project.swipetoplay.ui.components.LogoutDialog
import com.project.swipetoplay.ui.features.profile.ProfileScreen
import com.project.swipetoplay.ui.features.preferences.PreferencesScreen
import com.project.swipetoplay.ui.features.notifications.NotificationScreen
import com.project.swipetoplay.ui.features.game.GameDetailScreen
import com.project.swipetoplay.ui.features.home.HomeScreen
import com.project.swipetoplay.ui.features.details.DetailsScreen
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme
import com.project.swipetoplay.data.remote.api.RetrofitClient
import com.project.swipetoplay.ui.features.login.LoginScreen
import com.project.swipetoplay.ui.features.login.LoginViewModel
import com.project.swipetoplay.ui.features.login.LoginViewModelFactory
import com.project.swipetoplay.ui.features.onboarding.OnboardingScreen
import com.project.swipetoplay.data.local.OnboardingManager
import com.project.swipetoplay.data.local.GameCacheManager
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.ui.features.game.Game

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        RetrofitClient.initialize(applicationContext)
        
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
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val authManager = remember { GoogleAuthManager(context) }
    val onboardingManager = remember { OnboardingManager(context) }
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authManager)
    )
    
    // Create GameCacheManager instance
    val gameCacheManager = remember {
        GameCacheManager(
            context = context,
            recommendationRepository = RecommendationRepository()
        )
    }

    var currentScreen by remember { mutableStateOf("login") }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val navigationHistory = remember { mutableStateListOf<String>() }
    var isNavigatingBack by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val uiState by loginViewModel.uiState.collectAsState()
    val isAuthenticated = uiState.user != null
    
    var hasCompletedOnboarding by remember { mutableStateOf(onboardingManager.hasCompletedOnboarding()) }
    
    // Preload games when user is authenticated
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            hasCompletedOnboarding = onboardingManager.hasCompletedOnboarding()
            
            // Preload games cache
            val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
            if (tokenManager?.isAuthenticated() == true && !gameCacheManager.hasCachedGames() && !gameCacheManager.isPreloading()) {
                android.util.Log.d("MainActivity", "🔄 Starting game cache preload...")
                gameCacheManager.preloadGames(
                    scope = coroutineScope,
                    onSuccess = { games ->
                        android.util.Log.d("MainActivity", "✅ Successfully preloaded ${games.size} games")
                    },
                    onError = { exception ->
                        android.util.Log.e("MainActivity", "❌ Failed to preload games: ${exception.message}", exception)
                    }
                )
            }
        } else {
            // Clear cache when user logs out
            gameCacheManager.clearCache()
        }
    }
    
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated && currentScreen == "login") {
            currentScreen = "home"
            navigationHistory.clear()
            navigationHistory.add("home")
        }
    }
    
    LaunchedEffect(currentScreen) {
        if (!isNavigatingBack && currentScreen != "login" && currentScreen != "gameDetail") {
            val mainScreens = listOf("home", "profile", "details")
            if (currentScreen in mainScreens) {
                navigationHistory.remove(currentScreen)
                navigationHistory.add(currentScreen)
            }
        }
        isNavigatingBack = false
    }
    
    BackHandler(enabled = isAuthenticated && currentScreen != "login") {
        isNavigatingBack = true
        when {
            currentScreen == "gameDetail" -> {
                currentScreen = "home"
                selectedGame = null
            }
            currentScreen == "preferences" || 
            currentScreen == "notifications" -> {
                currentScreen = "profile"
            }
            else -> {
                if (navigationHistory.size > 1) {
                    navigationHistory.removeAt(navigationHistory.lastIndex)
                    val previousScreen = navigationHistory.lastOrNull()
                    if (previousScreen != null && previousScreen != currentScreen) {
                        currentScreen = previousScreen
                    }
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime > 2000) {
                        lastBackPressTime = currentTime
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Press twice to exit",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                        }
                    } else {
                        activity?.finish()
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when {
                    !isAuthenticated -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onNavigateToHome = { currentScreen = "home" }
                        )
                    }
                    currentScreen == "gameDetail" && selectedGame != null -> {
                        GameDetailScreen(
                            gameId = selectedGame!!.id.toIntOrNull() ?: 0,
                            onNavigateBack = {
                                currentScreen = "home"
                                selectedGame = null
                            }
                        )
                    }
                    else -> {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                onNavigateToDetails = { game ->
                                    selectedGame = game
                                    currentScreen = "gameDetail"
                                },
                                gameCacheManager = gameCacheManager
                            )
                            "profile" -> ProfileScreen(
                                user = uiState.user,
                                onNavigateToSettings = { currentScreen = "preferences" },
                                onNavigateToNotifications = { currentScreen = "notifications" },
                                onLogout = { showLogoutDialog = true }
                            )
                            "preferences" -> PreferencesScreen(
                                onNavigateBack = { currentScreen = "profile" },
                                onSaveChanges = { currentScreen = "profile" }
                            )
                            "notifications" -> NotificationScreen(
                                onNavigateBack = { currentScreen = "profile" },
                                onSaveChanges = { currentScreen = "profile" }
                            )
                            "details" -> DetailsScreen()
                            else -> HomeScreen(
                                onNavigateToDetails = { game ->
                                    selectedGame = game
                                    currentScreen = "gameDetail"
                                },
                                gameCacheManager = gameCacheManager
                            )
                        }
                    }
                }
            }

            LogoutDialog(
                isVisible = showLogoutDialog,
                onConfirm = {
                    showLogoutDialog = false
                    loginViewModel.signOut()
                    onboardingManager.resetOnboarding()
                    hasCompletedOnboarding = false
                    currentScreen = "login"
                },
                onDismiss = { showLogoutDialog = false }
            )

            if (isAuthenticated && currentScreen != "login" && currentScreen != "gameDetail") {
                BottomBar(
                    currentScreen = currentScreen,
                    onProfileClick = { currentScreen = "profile" },
                    onSwipeClick = { 
                        currentScreen = "home"
                        selectedGame = null
                    },
                    onDetailsClick = { currentScreen = "details" }
                )
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isAuthenticated && currentScreen != "login" && currentScreen != "gameDetail") 89.dp else 16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}