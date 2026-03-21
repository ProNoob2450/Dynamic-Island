package com.dynamicisland.core.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Pause
import androidx.compose.material3.icons.filled.PlayArrow
import androidx.compose.material3.icons.filled.SkipNext
import androidx.compose.material3.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MediaIslandCard(
    title: String,
    artist: String,
    isPlaying: Boolean,
    artwork: Bitmap?,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111113))
            .clickable(onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val albumModifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))

        if (artwork != null) {
            Image(
                bitmap = artwork.asImageBitmap(),
                contentDescription = "Album artwork",
                modifier = albumModifier,
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = albumModifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7B61FF), Color(0xFF22D3EE)),
                    )
                ),
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9EA2AD),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onPrevious, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
        }
        IconButton(onClick = onPlayPause, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
            )
        }
        IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
        }
    }
}
