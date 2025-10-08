package com.project.swipetoplay.ui.features.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.swipetoplay.ui.features.game.Game
import com.project.swipetoplay.ui.features.game.GameCard
import com.project.swipetoplay.ui.features.game.MockGameData
import com.project.swipetoplay.ui.theme.ProfileBackground
import com.project.swipetoplay.ui.theme.SwipeToPlayTheme
import kotlin.math.abs

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDetails: () -> Unit = {}
) {
    var currentGameIndex by remember { mutableIntStateOf(0) }
    var swipeCount by remember { mutableIntStateOf(0) }
    var isSwipeComplete by remember { mutableStateOf(false) }
    val games = MockGameData.allGames
    val currentGame = if (currentGameIndex < games.size) games[currentGameIndex] else null

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ProfileBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(onProfileClick = onNavigateToProfile)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentGame != null) {
                     val stackShuffleAnimation by animateFloatAsState(
                         targetValue = if (isSwipeComplete) 1f else 0f,
                         animationSpec = spring(
                             dampingRatio = Spring.DampingRatioLowBouncy,
                             stiffness = Spring.StiffnessVeryLow
                         ),
                         label = "stackShuffle"
                     )

                    CardStack(
                        remainingCards = games.size - currentGameIndex,
                        swipeCount = swipeCount,
                        shuffleProgress = stackShuffleAnimation
                    )

                      AnimatedContent(
                          targetState = currentGameIndex,
                          transitionSpec = {
                              (fadeIn(animationSpec = tween(250)) + slideInHorizontally(
                                  initialOffsetX = { -it / 3 },
                                  animationSpec = spring(
                                      dampingRatio = Spring.DampingRatioNoBouncy,
                                      stiffness = Spring.StiffnessHigh
                                  )
                              ) + scaleIn(
                                  initialScale = 0.9f,
                                  animationSpec = spring(
                                      dampingRatio = Spring.DampingRatioNoBouncy,
                                      stiffness = Spring.StiffnessHigh
                                  )
                              )) togetherWith
                                      (fadeOut(animationSpec = tween(250)) + slideOutHorizontally(
                                          targetOffsetX = { it / 3 },
                                          animationSpec = spring(
                                              dampingRatio = Spring.DampingRatioNoBouncy,
                                              stiffness = Spring.StiffnessHigh
                                          )
                                      ))
                          },
                          label = "cardTransition"
                      ) { key ->
                        val game = games.getOrNull(key)
                        if (game != null) {
                            SwipeableGameCard(
                                game = game,
                                key = key,
                                onSwipeLeft = {
                                    isSwipeComplete = true
                                },
                                onSwipeRight = {
                                    isSwipeComplete = true
                                },
                                onSwipeComplete = {
                                    currentGameIndex++
                                    swipeCount++
                                    isSwipeComplete = false
                                },
                                onViewClick = onNavigateToDetails
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No more games to swipe!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CardStack(
    remainingCards: Int,
    swipeCount: Int,
    shuffleProgress: Float = 0f
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

        val baseScale = 1.05f - (i * 0.01f)
        val baseOffsetX = pattern.first
        val baseOffsetY = pattern.second - (i * 8f)
        val baseRotation = pattern.third

         val shuffleVariation = (shuffleProgress * 0.15f) * (i * 0.05f + 0.05f)
         val shuffleRotation = shuffleProgress * 1f * (if (i % 2 == 0) 1f else -1f)

         val scale = baseScale + shuffleVariation
         val offsetX = baseOffsetX + (shuffleProgress * 2f * (if (i % 2 == 0) 1f else -1f))
         val offsetY = baseOffsetY + (shuffleProgress * 1.5f)
         val rotation = baseRotation + shuffleRotation

        val animatedScale by animateFloatAsState(scale, spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = "")
        val animatedOffsetX by animateFloatAsState(offsetX, spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = "")
        val animatedOffsetY by animateFloatAsState(offsetY, spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = "")
        val animatedRotation by animateFloatAsState(rotation, spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = "")

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(0.7f)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationY = animatedOffsetY,
                    translationX = animatedOffsetX,
                    rotationZ = animatedRotation
                )
                .alpha(0.9f - (i * 0.05f)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF240046),
            shadowElevation = 4.dp,
            border = BorderStroke(3.dp, Color(0xFF0D0D21))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (patternIndex % 3) {
                            0 -> Color(0xFF240046)
                            1 -> Color(0xFF1A0033)
                            else -> Color(0xFF2D0052)
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val random = kotlin.random.Random(patternIndex * 100)
                    repeat(200) {
                        val x = random.nextFloat() * size.width
                        val y = random.nextFloat() * size.height
                        val alpha = random.nextFloat() * 0.08f + 0.02f
                        val radius = random.nextFloat() * 1.5f + 0.3f
                        val colorVariation = when (random.nextInt(3)) {
                            0 -> Color.White.copy(alpha = alpha)
                            1 -> Color(0xFF7425B8).copy(alpha = alpha * 0.3f)
                            else -> Color(0xFF8FBFE6).copy(alpha = alpha * 0.2f)
                        }
                        drawCircle(
                            color = colorVariation,
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = Color(0xFF7425B8).copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF7425B8).copy(alpha = 0.1f)
                        )
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun TopBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Swipe To Play",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = "Help",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isAnimatingOut by remember { mutableStateOf(false) }

      LaunchedEffect(key) {
          offsetX = -100f
          offsetY = 0f
          isAnimatingOut = false
          isDragging = false
          animate(
              initialValue = -100f,
              targetValue = 0f,
              animationSpec = spring(
                  dampingRatio = Spring.DampingRatioNoBouncy,
                  stiffness = Spring.StiffnessHigh
              )
          ) { value, _ -> offsetX = value }
      }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = if (isAnimatingOut) spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ) else spring(),
        label = "",
        finishedListener = {
            if (isAnimatingOut && abs(it) > 1500f) {
                onSwipeComplete()
            }
        }
    )

    val animatedOffsetY by animateFloatAsState(targetValue = offsetY, spring(), label = "")
    val rotation = (animatedOffsetX / 25f).coerceIn(-15f, 15f)
    val entranceScale = (1f - (abs(animatedOffsetX) / 150f)).coerceIn(0.95f, 1f)
    val dragScale = if (isDragging) 0.97f else 1f
    val finalScale = minOf(entranceScale, dragScale)
     val cardOpacity = if (isAnimatingOut) {
         (1f - (abs(animatedOffsetX) / 1800f)).coerceIn(0f, 1f)
     } else {
         val baseOpacity = (1f - (abs(animatedOffsetX) / 2000f)).coerceIn(0.85f, 1f)
         val entranceOpacity = (1f - (abs(animatedOffsetX) / 100f)).coerceIn(0.8f, 1f)
         minOf(baseOpacity, entranceOpacity)
     }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(key) {
                detectDragGestures(
                    onDragStart = { if (!isAnimatingOut) isDragging = true },
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
                    if (!isAnimatingOut) {
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y * 0.4f
                    }
                }
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .align(Alignment.Center)
                .graphicsLayer(
                    translationX = animatedOffsetX,
                    translationY = animatedOffsetY,
                    rotationZ = rotation,
                    scaleX = finalScale,
                    scaleY = finalScale,
                    alpha = cardOpacity
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(3.dp, Color(0xFF0D0D21)),
            shadowElevation = 4.dp
        ) {
            GameCard(game = game, onViewClick = onViewClick, modifier = Modifier.fillMaxSize())
        }

        if (!isAnimatingOut || abs(animatedOffsetX) < 500f) {
            SwipeOverlays(offsetX = animatedOffsetX, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SwipeOverlays(offsetX: Float, modifier: Modifier = Modifier) {
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
                    text = "NAH",
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
                    text = "YEAH",
                    color = Color(0xFF4CAF50),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
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
