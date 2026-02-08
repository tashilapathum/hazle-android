package com.tashila.hazle.ui.components.paywall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tashila.hazle.R

@Composable
fun ComparisonTable(modifier: Modifier = Modifier) {

    val featureItems = remember {
        listOf(
            ComparisonItem(R.string.paywall_feature_ai_models, R.string.paywall_feature_ai_models_free, R.string.paywall_feature_ai_models_pro, R.string.paywall_feature_ai_models_byok, isBold = true),
            ComparisonItem(R.string.paywall_feature_daily_chats, R.string.paywall_feature_daily_chats_free, R.string.paywall_feature_daily_chats_pro, R.string.paywall_feature_daily_chats_byok, isBold = true),
            ComparisonItem(R.string.paywall_feature_cloud_sync, R.string.paywall_feature_cloud_sync_free, R.string.paywall_feature_cloud_sync_pro, R.string.paywall_feature_cloud_sync_byok, isBold = true),
            ComparisonItem(R.string.paywall_feature_api_cost, R.string.paywall_feature_api_cost_free, R.string.paywall_feature_api_cost_pro, R.string.paywall_feature_api_cost_byok, isBold = false),
            ComparisonItem(R.string.paywall_feature_updates, R.string.paywall_feature_updates_free, R.string.paywall_feature_updates_pro, R.string.paywall_feature_updates_byok, isBold = false)
        )
    }

    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.paywall_feature_comparison_header_feature), modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.paywall_feature_comparison_header_free), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.paywall_feature_comparison_header_pro), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(id = R.string.paywall_feature_comparison_header_byok), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            featureItems.forEach { item ->
                FeatureRow(item = item)
            }
        }
    }
}

@Composable
private fun FeatureRow(item: ComparisonItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = item.feature), modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)

        val fontWeight = if (item.isBold) FontWeight.Bold else FontWeight.Normal

        Text(stringResource(id = item.free), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, fontWeight = fontWeight)
        Text(stringResource(id = item.pro), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = fontWeight)
        Text(stringResource(id = item.byok), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, fontWeight = fontWeight)
    }
}

private data class ComparisonItem(
    val feature: Int,
    val free: Int,
    val pro: Int,
    val byok: Int,
    val isBold: Boolean = false
)