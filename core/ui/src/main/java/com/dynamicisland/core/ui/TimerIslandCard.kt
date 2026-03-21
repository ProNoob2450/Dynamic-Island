package com.dynamicisland.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TimerCompactPill(remainingSeconds: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF151516))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatCountdown(remainingSeconds), color = Color.White)
    }
}

@Composable
fun TimerIslandCard(label: String, remainingSeconds: Long, isRunning: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111113))
            .padding(14.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Text(
            formatCountdown(remainingSeconds),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )
        Text(if (isRunning) "Running" else "Stopped", color = Color(0xFF9EA2AD))
    }
}

private fun formatCountdown(seconds: Long): String {
    val m = (seconds / 60).coerceAtLeast(0)
    val s = (seconds % 60).coerceAtLeast(0)
    return "%02d:%02d".format(m, s)
}
