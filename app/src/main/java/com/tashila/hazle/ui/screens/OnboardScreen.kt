package com.tashila.hazle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tashila.hazle.features.onboard.pages
import com.tashila.hazle.ui.components.onboard.BubblePager
import com.tashila.hazle.ui.components.onboard.shimmerEffect

@Composable
fun OnboardingScreen(pagerState: PagerState, onboardingFinished: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        BubblePager(
            pagerState = pagerState,
            pageCount = pages.size,
            modifier = Modifier.fillMaxSize(),
            bubbleColors = pages.map { it.color.copy(alpha = 0.2f) },
            onFinished = { onboardingFinished.invoke() }
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = pages[page].content.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(60.dp))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pages[page].content.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = pages[page].content.text,
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
                    text = pages[page].content.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}