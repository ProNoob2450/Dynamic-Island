package com.dynamicisland.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Call
import androidx.compose.material3.icons.filled.CallEnd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CallCompactPill(
    callerName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF151516))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759)),
        )
        Text(
            text = callerName,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun CallIslandCard(
    callerName: String,
    callerNumber: String,
    durationLabel: String,
    showAnswer: Boolean,
    onAnswer: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111113))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = callerName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = callerNumber,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9EA2AD),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = durationLabel,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7CE38B),
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (showAnswer) {
                IconButton(
                    onClick = onAnswer,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1FA855)),
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Answer", tint = Color.White)
                }
            }
            IconButton(
                onClick = onEnd,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD62C2C)),
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End call", tint = Color.White)
            }
        }
    }
}
