package com.tashila.hazle.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tashila.hazle.R
import com.tashila.hazle.ui.components.onboard.PageContent
import com.tashila.hazle.ui.components.onboard.pages
import com.tashila.hazle.ui.components.onboard.AnimatedWhisperText
import com.tashila.hazle.ui.components.onboard.BubblePager
import com.tashila.hazle.ui.components.onboard.shimmerEffect
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(pagerState: PagerState, onboardingFinished: () -> Unit) {
    val density = LocalDensity.current
    val context = LocalContext.current
    var parentBoxHeightPx by remember { mutableIntStateOf(0) }
    var onboardingState by remember { mutableStateOf(OnboardingState.InitialTextCentered) }
    val transition = updateTransition(onboardingState, label = "OnboardingTransition")

    // Animate the vertical bias for alignment in the Box
    val whisperTextVerticalBias by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000) }, // Adjust duration as needed
        label = "WhisperTextVerticalBias"
    ) { state ->
        when (state) {
            OnboardingState.InitialTextCentered -> 0f // Center (default Box alignment)
            OnboardingState.AnimatingToTop, OnboardingState.TitleAtTopAndContentVisible -> -0.75f // Adjust to move towards the top
        }
    }

    // Animate the font size
    val whisperTextFontSize by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000) },
        label = "WhisperTextFontSize"
    ) { state ->
        when (state) {
            OnboardingState.InitialTextCentered -> 128f // Large size
            OnboardingState.AnimatingToTop, OnboardingState.TitleAtTopAndContentVisible -> 48f // Smaller title size
        }
    }

    // Convert animated float to sp
    val animatedWhisperTextFontSize = with(density) { whisperTextFontSize.sp }

    // Preload images and trigger animation
    LaunchedEffect(Unit) {
        // Preload all images for the onboarding pages in the background
        pages.forEach { page ->
            val request = ImageRequest.Builder(context)
                .data(page.content.imageUrl)
                .build()
            ImageLoader(context).enqueue(request) // No need to await here, just start loading
        }

        // Delay and then start the animation
        launch {
            kotlinx.coroutines.delay(1000) // Initial delay before animation starts
            onboardingState = OnboardingState.AnimatingToTop
        }
    }

    // Monitor transition state to know when animation is complete
    LaunchedEffect(transition.currentState, transition.targetState) {
        if (transition.currentState == OnboardingState.AnimatingToTop &&
            transition.targetState == OnboardingState.AnimatingToTop &&
            !transition.isRunning
        ) {
            // The animation to 'AnimatingToTop' has completed and is idle
            onboardingState = OnboardingState.TitleAtTopAndContentVisible
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                parentBoxHeightPx = layoutCoordinates.size.height
            },
        contentAlignment = Alignment.Center // Default center alignment for the Box
    ) {
        // --- AnimatedWhisperText ---
        val whisperTypeface = ResourcesCompat.getFont(context, R.font.whisper)
        AnimatedWhisperText(
            text = "Hazle",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.Center) // Handles the initial position,
                .offset(y = with(density) {
                    // Calculate the offset based on the animated verticalBias.
                    // A positive bias moves down, negative moves up.
                    // The bias is a fraction of the parent's height.
                    val offsetPx = parentBoxHeightPx * (whisperTextVerticalBias / 2f)
                    offsetPx.toDp()
                }),
            fontSize = animatedWhisperTextFontSize,
            color = pages[pagerState.currentPage].color, // Use current page color
            customTypeface = whisperTypeface
        )

        // --- BubblePager (Conditionally Visible) ---
        // Apply alpha to make it invisible until the animation is done
        Box(
            modifier = Modifier
                .fillMaxSize() // Use offset to move it off-screen and then back
                .offset(y = if (onboardingState == OnboardingState.TitleAtTopAndContentVisible) 0.dp else 1000.dp)
                .wrapContentSize(align = Alignment.TopCenter)
        ) {
            AnimatedVisibility(
                visible = onboardingState == OnboardingState.TitleAtTopAndContentVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight / 4 } // Slide in from slightly above
                ),
                exit = fadeOut()
            ) {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                BubblePager(
                    pagerState = pagerState,
                    pageCount = pages.size,
                    modifier = Modifier.fillMaxSize(),
                    bubbleColors = pages.map { it.color.copy(alpha = 0.2f) },
                    onFinished = { onboardingFinished.invoke() }
                ) { pageIndex ->
                    val currentPage = pages[pageIndex]

                    if (isLandscape) {
                        LandscapePageContent(currentPage.content)
                    } else {
                        PortraitPageContent(currentPage.content)
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitPageContent(content: PageContent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = stringResource(content.titleResId),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            softWrap = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(60.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(content.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(content.textResId),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .height(300.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect(),
            contentScale = ContentScale.Inside,
            error = rememberVectorPainter(Icons.Outlined.BrokenImage)
        )
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(content.textResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            softWrap = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun LandscapePageContent(content: PageContent) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(content.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(content.textResId),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(300.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect(),
            contentScale = ContentScale.Inside,
            error = rememberVectorPainter(Icons.Outlined.BrokenImage)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(content.titleResId),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(content.textResId),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

enum class OnboardingState {
    InitialTextCentered,
    AnimatingToTop,
    TitleAtTopAndContentVisible
}