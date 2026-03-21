package com.dynamicisland.core.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IslandExpandedCard(
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
    Box(modifier = modifier) {
        MediaIslandCard(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            artwork = artwork,
            onTap = onTap,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            modifier = Modifier.matchParentSize(),
        )
    }
}
