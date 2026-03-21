package com.dynamicisland.core.overlay

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.dynamicisland.core.events.UserGesture

class IslandGestureController(
    private val onGesture: (UserGesture) -> Unit,
) {
    fun modifier(): Modifier {
        val tapModifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { onGesture(UserGesture.Tap) },
                onLongPress = { onGesture(UserGesture.LongPress) },
            )
        }
        val swipeModifier = Modifier.pointerInput(Unit) {
            var totalDy = 0f
            detectVerticalDragGestures(
                onDragStart = { totalDy = 0f },
                onVerticalDrag = { _, dragAmount -> totalDy += dragAmount },
                onDragEnd = {
                    when {
                        totalDy <= -80f -> onGesture(UserGesture.SwipeUpDismiss)
                        totalDy >= 80f -> onGesture(UserGesture.SwipeDownPeek)
                    }
                },
            )
        }
        return tapModifier.then(swipeModifier)
    }
}
