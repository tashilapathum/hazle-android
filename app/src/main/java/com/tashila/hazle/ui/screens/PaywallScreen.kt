package com.tashila.hazle.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tashila.hazle.R
import com.tashila.hazle.ui.components.paywall.ComparisonTable
import com.tashila.hazle.ui.components.paywall.FinePrintText
import com.tashila.hazle.ui.components.paywall.IndieDeveloperNote
import com.tashila.hazle.ui.components.paywall.Plan
import com.tashila.hazle.ui.components.paywall.PlanCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PaywallScreen(
    onBackClicked: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState(initialPage = 1) { 3 }
    var showPaywall by remember { mutableStateOf(true) }

    val plans = remember {
        listOf(
            Plan(
                title = R.string.paywall_free_tier_title,
                price = R.string.paywall_free_tier_price,
                features = listOf(
                    R.string.paywall_free_feature_1,
                    R.string.paywall_free_feature_2,
                    R.string.paywall_free_feature_3,
                    R.string.paywall_free_feature_4
                ),
                ctaText = R.string.paywall_free_tier_cta,
                isCtaEnabled = false
            ),
            Plan(
                title = R.string.paywall_pro_tier_title,
                price = R.string.paywall_pro_tier_price,
                subtitle = R.string.paywall_pro_tier_subtitle,
                features = listOf(
                    R.string.paywall_pro_feature_1,
                    R.string.paywall_pro_feature_2,
                    R.string.paywall_pro_feature_3
                ),
                ctaText = R.string.paywall_pro_tier_cta,
                isRecommended = true
            ),
            Plan(
                title = R.string.paywall_byok_tier_title,
                price = R.string.paywall_byok_tier_price,
                subtitle = R.string.paywall_byok_tier_subtitle,
                features = listOf(
                    R.string.paywall_byok_feature_1,
                    R.string.paywall_byok_feature_2,
                    R.string.paywall_byok_feature_3,
                    R.string.paywall_byok_feature_4,
                ),
                ctaText = R.string.paywall_byok_tier_cta
            )
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.paywall_main_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.paywall_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.padding(top = 24.dp)
                ) { page ->
                    val plan = plans[page]
                    PlanCard(
                        plan = plan,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(id = R.string.paywall_feature_comparison_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                ComparisonTable(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                IndieDeveloperNote()
            }

            item {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinePrintText(
                        stringResource(id = R.string.paywall_fine_print_subscription)
                    )
                    FinePrintText(
                        stringResource(id = R.string.paywall_fine_print_byok)
                    )
                }
            }
        }

        if (showPaywall) {

        }
    }
}
