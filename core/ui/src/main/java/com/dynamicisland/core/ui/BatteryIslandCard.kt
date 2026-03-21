package com.dynamicisland.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.BatteryChargingFull
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BatteryCompactPill(percent: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF151516))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF7CE38B))
        Text(" $percent%", color = Color.White)
    }
}

@Composable
fun BatteryIslandCard(percent: Int, isCharging: Boolean, etaToFull: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111113))
            .padding(14.dp),
    ) {
        Text("Battery", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text("$percent%", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF7CE38B))
        Text(
            if (isCharging) "Charging • Full in $etaToFull" else "Not charging",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9EA2AD),
        )
    }
}
