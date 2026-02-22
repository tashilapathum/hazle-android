package com.tashila.hazle.ui.components.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tashila.hazle.R

@Composable
fun PlanCard(
    plan: Plan,
    modifier: Modifier = Modifier,
    onCtaClicked: () -> Unit,
) {
    Card(
        modifier = modifier.height(IntrinsicSize.Max),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (plan.isRecommended) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (plan.isRecommended) {
                Text(
                    text = stringResource(id = R.string.paywall_recommended),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(stringResource(id = plan.title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (plan.price != null) {
                    Text(plan.price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                plan.subtitle?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plan.features.forEach { featureRes ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = featureRes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (plan.isCtaEnabled) {
                Button(
                    onClick = onCtaClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (plan.isRecommended) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    border = if (!plan.isRecommended) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text(stringResource(id = plan.ctaText))
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                ) {
                    Text(stringResource(id = plan.ctaText))
                }
            }
        }
    }
}
