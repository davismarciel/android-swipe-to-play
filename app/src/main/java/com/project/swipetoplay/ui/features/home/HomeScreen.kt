package com.project.swipetoplay.ui.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.swipetoplay.data.repository.InteractionRepository
import com.project.swipetoplay.data.repository.RecommendationRepository
import com.project.swipetoplay.ui.features.game.Game
import com.project.swipetoplay.ui.features.game.GameCard
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme
import com.project.swipetoplay.ui.components.TopBar
import kotlin.math.abs
import kotlinx.coroutines.delay

private const val ENTRANCE_ANIMATION_DURATION_MS = 650L
private const val ENTRANCE_DELAY_MS = 20L
private const val FLIP_ROTATION_DEGREES = 90f
private const val SPRING_DAMPING = Spring.DampingRatioMediumBouncy
private const val SPRING_STIFFNESS = Spring.StiffnessMediumLow

private const val TAP_ANIMATION_DURATION_MS = 200
private const val EXIT_ANIMATION_DURATION_MS = 300
private const val TAP_SCALE_DOWN = 0.94f
private const val TAP_ROTATION_Y = 5f
private const val TAP_TRANSLATION_Z = -60f

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetails: (Game) -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            recommendationRepository = RecommendationRepository(),
            interactionRepository = InteractionRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var swipeCount by remember { mutableIntStateOf(0) }
    var hasAttemptedLoad by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    val currentGame = viewModel.getCurrentGame()

    LaunchedEffect(Unit) {
        if (!hasAttemptedLoad) {
            delay(800)

            val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
            if (tokenManager?.isAuthenticated() == true) {
                com.project.swipetoplay.data.error.ErrorLogger.logDebug("HomeScreen", "Token verified, loading recommendations")
                if (uiState.games.isEmpty() && !uiState.isLoading) {
                    hasAttemptedLoad = true
                    viewModel.loadRecommendations()
                }
            } else {
                com.project.swipetoplay.data.error.ErrorLogger.logWarning("HomeScreen", "No token available yet, will retry", null)
                delay(500)
                val retryTokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
                if (retryTokenManager?.isAuthenticated() == true && uiState.games.isEmpty() && !uiState.isLoading) {
                    com.project.swipetoplay.data.error.ErrorLogger.logDebug("HomeScreen", "Token verified on retry, loading recommendations")
                    hasAttemptedLoad = true
                    viewModel.loadRecommendations()
                }
            }
        }
    }

    LaunchedEffect(uiState.games.isEmpty(), uiState.isLoading) {
        if (uiState.games.isEmpty() && !uiState.isLoading && !hasAttemptedLoad) {
            val tokenManager = com.project.swipetoplay.data.remote.api.RetrofitClient.getTokenManager()
            if (tokenManager?.isAuthenticated() == true) {
                com.project.swipetoplay.data.error.ErrorLogger.logDebug("HomeScreen", "Games list empty, loading recommendations")
                hasAttemptedLoad = true
                viewModel.loadRecommendations()
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ProfileBackground
    ) {
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
                TopBar(onHelpClick = { showTutorial = true })

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        else -> {
                            val errorMessage = uiState.error
                            when {
                                errorMessage != null -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = errorMessage,
                                            color = Color.Red,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        Button(onClick = { viewModel.retry() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                                uiState.hasReachedDailyLimit -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.9f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color(0xFFFFA726),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Daily Limit Reached",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "You've reached your daily limit of 20 games.\nCome back tomorrow!",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 24.sp
                                            )
                                        }
                                    }
                                }
                                currentGame != null -> {
                                    CardStack(
                                        remainingCards = uiState.games.size - uiState.currentGameIndex,
                                        swipeCount = swipeCount
                                    )

                                    SwipeableGameCard(
                                        game = currentGame,
                                        key = uiState.currentGameIndex,
                                        onSwipeLeft = {
                                            viewModel.onSwipeLeft(currentGame)
                                        },
                                        onSwipeRight = {
                                            viewModel.onSwipeRight(currentGame)
                                        },
                                        onSwipeComplete = {
                                            swipeCount++
                                            viewModel.moveToNextGame()
                                        },
                                        onViewClick = {
                                            viewModel.onGameViewed(currentGame)
                                            onNavigateToDetails(currentGame)
                                        }
                                    )
                                }
                                else -> {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.9f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Gamepad,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.6f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "No more games to swipe!",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (uiState.remainingGames > 0) {
                                                Text(
                                                    text = "Remaining today: ${uiState.remainingGames}",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 16.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            } else {
                                                Text(
                                                    text = "Check back later for more recommendations",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 16.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!uiState.hasReachedDailyLimit && !uiState.isLoading && uiState.remainingGames > 0 && uiState.games.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Games remaining today: ${uiState.remainingGames}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            }
            
            if (showTutorial) {
                SwipeTutorialDialog(
                    onDismiss = { showTutorial = false }
                )
            }
        }
    }
}

@Composable
private fun CardStack(
    remainingCards: Int,
    swipeCount: Int
) {
    val maxStackCards = 4
    val cardsToShow = minOf(remainingCards - 1, maxStackCards)

    val cardPatterns = listOf(
        Triple(-8f, -8f, -3f),
        Triple(12f, -12f, 4f),
        Triple(-5f, -18f, -2f),
        Triple(8f, -22f, 3f)
    )

    for (i in cardsToShow downTo 1) {
        val patternIndex = (swipeCount + i - 1) % cardPatterns.size
        val pattern = cardPatterns[patternIndex]

        val scale = 1.02f - (i * 0.015f)
        val offsetX = pattern.first
        val offsetY = pattern.second - (i * 8f)
        val rotation = pattern.third

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(0.7f)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationY = offsetY,
                    translationX = offsetX,
                    rotationZ = rotation
                )
                .alpha(0.9f - (i * 0.05f)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1A1A1A),
            shadowElevation = (12 - i * 2).dp,
            border = BorderStroke(
                width = 3.dp,
                color = Color(0xFF0D0D0D)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            .background(
                when (patternIndex % 3) {
                            0 -> Color(0xFF1F1F1F)
                            1 -> Color(0xFF1C1C1C)
                            else -> Color(0xFF212121)
                        }
                    )
                ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val random = kotlin.random.Random(patternIndex * 100)
                    repeat(150) {
                        val x = random.nextFloat() * size.width
                        val y = random.nextFloat() * size.height
                        val alpha = random.nextFloat() * 0.15f
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = random.nextFloat() * 2f + 0.5f,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {}
                }
            }
        }
    }
}


@Composable
private fun SwipeableGameCard(
    game: Game,
    key: Int,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeComplete: () -> Unit,
    onViewClick: () -> Unit
) {
    var offsetX by remember(key) { mutableFloatStateOf(0f) }
    var offsetY by remember(key) { mutableFloatStateOf(0f) }
    var isDragging by remember(key) { mutableStateOf(false) }
    var isAnimatingOut by remember(key) { mutableStateOf(false) }
    var isAnimatingIn by remember(key) { mutableStateOf(true) }
    var isClicking by remember(key) { mutableStateOf(false) }
    var isTapPressed by remember(key) { mutableStateOf(false) }

    var entranceRotationY by remember(key) { mutableFloatStateOf(180f) }
    var entranceScale by remember(key) { mutableFloatStateOf(0.88f) }
    var entranceTranslationZ by remember(key) { mutableFloatStateOf(-150f) }
    var clickScale by remember(key) { mutableFloatStateOf(1f) }
    var clickAlpha by remember(key) { mutableFloatStateOf(1f) }
    var clickTranslationZ by remember(key) { mutableFloatStateOf(0f) }
    var clickRotationY by remember(key) { mutableFloatStateOf(0f) }

    LaunchedEffect(key) {
        offsetX = 0f
        offsetY = 0f
        isAnimatingOut = false
        isDragging = false
        isAnimatingIn = true
        isClicking = false
        isTapPressed = false

        entranceRotationY = 180f
        entranceScale = 0.88f
        entranceTranslationZ = -150f
        clickScale = 1f
        clickAlpha = 1f
        clickTranslationZ = 0f
        clickRotationY = 0f

        delay(ENTRANCE_DELAY_MS)

        entranceRotationY = 0f
        entranceScale = 1f
        entranceTranslationZ = 0f

        delay(ENTRANCE_ANIMATION_DURATION_MS)
        isAnimatingIn = false
    }

    val animatedClickScale by animateFloatAsState(
        targetValue = clickScale,
        animationSpec = tween(
            durationMillis = TAP_ANIMATION_DURATION_MS,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "clickScale"
    )

    val animatedClickAlpha by animateFloatAsState(
        targetValue = clickAlpha,
        animationSpec = tween(
            durationMillis = TAP_ANIMATION_DURATION_MS,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "clickAlpha",
        finishedListener = {
            if (isClicking && it <= 0.05f) {
                onViewClick()
                isClicking = false
                isTapPressed = false
                clickScale = 1f
                clickAlpha = 1f
                clickTranslationZ = 0f
                clickRotationY = 0f
            }
        }
    )

    val animatedClickTranslationZ by animateFloatAsState(
        targetValue = clickTranslationZ,
        animationSpec = tween(
            durationMillis = TAP_ANIMATION_DURATION_MS,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "clickTranslationZ"
    )

    val animatedClickRotationY by animateFloatAsState(
        targetValue = clickRotationY,
        animationSpec = tween(
            durationMillis = TAP_ANIMATION_DURATION_MS,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "clickRotationY"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = if (isAnimatingOut) {
            tween(
                durationMillis = EXIT_ANIMATION_DURATION_MS,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "animatedOffsetX",
        finishedListener = {
            if (isAnimatingOut && abs(it) > 1500f) {
                onSwipeComplete()
            }
        }
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedOffsetY"
    )

    // Smooth easing for card flip - starts fast, ends smoothly with subtle overshoot
    val flipEasing = CubicBezierEasing(0.34f, 1.2f, 0.64f, 1f)
    
    val animatedEntranceRotationY by animateFloatAsState(
        targetValue = entranceRotationY,
        animationSpec = tween(
            durationMillis = ENTRANCE_ANIMATION_DURATION_MS.toInt(),
            easing = flipEasing
        ),
        label = "animatedEntranceRotationY"
    )

    val animatedEntranceScale by animateFloatAsState(
        targetValue = entranceScale,
        animationSpec = tween(
            durationMillis = ENTRANCE_ANIMATION_DURATION_MS.toInt(),
            easing = flipEasing
        ),
        label = "animatedEntranceScale"
    )

    val animatedEntranceTranslationZ by animateFloatAsState(
        targetValue = entranceTranslationZ,
        animationSpec = tween(
            durationMillis = ENTRANCE_ANIMATION_DURATION_MS.toInt(),
            easing = flipEasing
        ),
        label = "animatedEntranceTranslationZ"
    )

    val rotationZ = if (!isAnimatingIn) {
        (animatedOffsetX / 25f).coerceIn(-15f, 15f)
    } else {
        0f
    }

    val rotationY = if (isAnimatingIn) {
        animatedEntranceRotationY
    } else if (isClicking) {
        animatedClickRotationY
    } else if (isAnimatingOut) {
        val progress = (abs(animatedOffsetX) / 1800f).coerceIn(0f, 1f)
        if (animatedOffsetX > 0) {
            FLIP_ROTATION_DEGREES * progress
        } else {
            -FLIP_ROTATION_DEGREES * progress
        }
    } else {
        0f
    }

    val dragScale = if (isDragging) 0.98f else 1f

    val entranceScaleEffect = if (isAnimatingIn) {
        animatedEntranceScale
    } else {
        1f
    }

    val flipScaleX = if (abs(rotationY) > 0) {
        // Create a more realistic 3D perspective effect during flip
        // When card is edge-on (90°), scale should be minimal
        // When card is facing viewer (0° or 180°), scale should be full
        val angleRad = Math.toRadians(abs(rotationY).toDouble())
        // Use sine for edge-on effect: sin(0°) = 0, sin(90°) = 1, sin(180°) = 0
        // We want: scale = 1 when angle is 0° or 180°, scale = min when angle is 90°
        // So: scale = 1 - (1 - minScale) * sin²(angle)
        val sinValue = kotlin.math.sin(angleRad).toFloat()
        val minScale = 0.25f // Minimum scale when completely edge-on
        (1f - (1f - minScale) * sinValue * sinValue).coerceIn(minScale, 1f)
    } else {
        1f
    }

    val cardOpacity = when {
        isClicking -> animatedClickAlpha
        isAnimatingOut -> {
            (1f - (abs(animatedOffsetX) / 1800f)).coerceIn(0f, 1f)
        }
        isAnimatingIn -> {
            // During flip: more opaque when facing viewer (0° or 180°), less at 90° (edge-on)
            val angleRad = Math.toRadians(animatedEntranceRotationY.toDouble())
            // Use absolute value of cosine to get opacity based on viewing angle
            val opacityFactor = kotlin.math.abs(kotlin.math.cos(angleRad)).toFloat()
            // Start from 0.5 opacity at 180°, reach 1.0 at 0° for smoother transition
            (0.5f + opacityFactor * 0.5f).coerceIn(0.5f, 1f)
        }
        else -> {
            (1f - (abs(animatedOffsetX) / 2000f)).coerceIn(0.85f, 1f)
        }
    }

    val finalScale = if (isClicking) {
        animatedClickScale * dragScale * flipScaleX * entranceScaleEffect
    } else {
        dragScale * flipScaleX * entranceScaleEffect
    }

    val cameraDistance = when {
        isClicking -> 12f + (animatedClickTranslationZ / -8f)
        isAnimatingIn -> {
            // Increase camera distance during entrance for better depth effect
            val depthFactor = (animatedEntranceTranslationZ / -150f).coerceIn(0f, 1f)
            12f + (depthFactor * 6f)
        }
        else -> 12f
    }

    val shadowElevation = if (isClicking) {
        (16f * animatedClickAlpha).dp
    } else if (isDragging) {
        20.dp
    } else {
        16.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        if (!isAnimatingOut && !isAnimatingIn && !isDragging && !isClicking) {
                            isTapPressed = true
                            clickScale = TAP_SCALE_DOWN
                            clickRotationY = TAP_ROTATION_Y
                            clickTranslationZ = TAP_TRANSLATION_Z

                            tryAwaitRelease()

                            if (isTapPressed) {
                                isClicking = true
                                clickAlpha = 0f
                            }
                        }
                    }
                )
            }
            .pointerInput(key) {
                detectDragGestures(
                    onDragStart = {
                        if (!isAnimatingOut && !isAnimatingIn) {
                            isDragging = true
                            if (isTapPressed) {
                                isTapPressed = false
                                clickScale = 1f
                                clickAlpha = 1f
                                clickTranslationZ = 0f
                                clickRotationY = 0f
                            }
                        }
                    },
                    onDragEnd = {
                        if (!isAnimatingOut) {
                            isDragging = false
                            val swipeThreshold = 250f

                            when {
                                offsetX > swipeThreshold -> {
                                    isAnimatingOut = true
                                    offsetX = 1800f
                                    offsetY += 300f
                                    onSwipeRight()
                                }
                                offsetX < -swipeThreshold -> {
                                    isAnimatingOut = true
                                    offsetX = -1800f
                                    offsetY += 300f
                                    onSwipeLeft()
                                }
                                else -> {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        if (!isAnimatingOut) {
                            isDragging = false
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                ) { _, dragAmount ->
                    if (!isAnimatingOut && !isAnimatingIn) {
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y * 0.4f
                    }
                }
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(1f)
                .aspectRatio(0.7f)
                .align(Alignment.Center)
                .graphicsLayer(
                    translationX = animatedOffsetX,
                    translationY = animatedOffsetY,
                    rotationZ = rotationZ,
                    rotationY = rotationY,
                    scaleX = finalScale,
                    scaleY = finalScale,
                    alpha = cardOpacity,
                    transformOrigin = TransformOrigin.Center,
                    cameraDistance = cameraDistance
                ),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(3.dp, Color(0xFF0D0D0D)),
            shadowElevation = shadowElevation
        ) {
            GameCard(
                game = game,
                onViewClick = {
                    if (!isAnimatingOut && !isAnimatingIn && !isDragging && !isClicking) {
                        isTapPressed = true
                        clickScale = TAP_SCALE_DOWN
                        clickRotationY = TAP_ROTATION_Y
                        clickTranslationZ = TAP_TRANSLATION_Z
                        isClicking = true
                        clickAlpha = 0f
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isAnimatingOut || abs(animatedOffsetX) < 500f) {
            SwipeOverlays(
                offsetX = animatedOffsetX,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SwipeOverlays(
    offsetX: Float,
    modifier: Modifier = Modifier
) {
    val swipeThreshold = 50f
    val maxOpacity = 0.95f

    val rightOpacity = ((offsetX - swipeThreshold) / 300f).coerceIn(0f, maxOpacity)
    val leftOpacity = ((abs(offsetX) - swipeThreshold) / 300f).coerceIn(0f, maxOpacity)

    Box(modifier = modifier.padding(32.dp)) {
        if (offsetX < -swipeThreshold) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .rotate(-20f)
                    .alpha(leftOpacity)
                    .scale(0.9f + (leftOpacity * 0.1f)),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(5.dp, Color(0xFFF44336))
            ) {
                Text(
                    text = "DISLIKE",
                    color = Color(0xFFF44336),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
        if (offsetX > swipeThreshold) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .rotate(20f)
                    .alpha(rightOpacity)
                    .scale(0.9f + (rightOpacity * 0.1f)),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(5.dp, Color(0xFF4CAF50))
            ) {
                Text(
                    text = "LIKE",
                    color = Color(0xFF4CAF50),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}


@Composable
private fun SwipeTutorialDialog(onDismiss: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf("like", "dislike")
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentStep = (currentStep + 1) % steps.size
            delay(500)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How to use",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                SwipeTutorialCard(
                    direction = steps[currentStep],
                    modifier = Modifier
                        .width(300.dp)
                        .height(400.dp)
                )
                
                when (steps[currentStep]) {
                    "like" -> {
                        Text(
                            text = "Swipe right",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "to LIKE",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 18.sp
                            )
                        }
                    }
                    "dislike" -> {
                        Text(
                            text = "Swipe left",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "to DISLIKE",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                
                Text(
                    text = "You can also tap on the card to view details",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7425B8)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 40.dp, max = 40.dp)
                ) {
                    Text(
                        text = "Got it!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp
    )
}

@Composable
private fun SwipeTutorialCard(
    direction: String,
    modifier: Modifier = Modifier
) {
    var offsetX by remember(direction) { mutableFloatStateOf(0f) }
    var rotation by remember(direction) { mutableFloatStateOf(0f) }
    var alpha by remember(direction) { mutableFloatStateOf(1f) }
    
    LaunchedEffect(direction) {
        while (true) {
            offsetX = 0f
            rotation = 0f
            alpha = 1f
            delay(800)
            
            when (direction) {
                "like" -> {
                    offsetX = 400f
                    rotation = 20f
                    alpha = 0.3f
                }
                "dislike" -> {
                    offsetX = -400f
                    rotation = -20f
                    alpha = 0.3f
                }
            }
            delay(1500)
        }
    }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorialOffsetX"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorialRotation"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(300),
        label = "tutorialAlpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (direction) {
            "like" -> {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50).copy(alpha = 0.3f),
                    modifier = Modifier.size(120.dp)
                )
            }
            "dislike" -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFE53935).copy(alpha = 0.3f),
                    modifier = Modifier.size(120.dp)
                )
            }
        }
        
        Card(
            modifier = Modifier
                .graphicsLayer {
                    translationX = animatedOffsetX
                    rotationZ = animatedRotation
                    alpha = animatedAlpha
                }
                .fillMaxSize(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Example Game",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SwipeToPlayTheme {
        HomeScreen()
    }
}