package com.dynamicisland.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class IslandMotionController internal constructor(
    private val morphAnim: Animatable<Float, AnimationVector1D>,
    private val secondaryBubbleAnim: Animatable<Float, AnimationVector1D>,
    private val replaceAnim: Animatable<Float, AnimationVector1D>,
    private val dismissAnim: Animatable<Float, AnimationVector1D>,
) {
    val progress: Float get() = morphAnim.value
    val secondaryBubbleAlpha: Float get() = secondaryBubbleAnim.value
    val replaceProgress: Float get() = replaceAnim.value
    val dismissProgress: Float get() = dismissAnim.value

    var currentTarget by mutableFloatStateOf(0f)
        private set

    suspend fun animateMorph(expanded: Boolean) {
        val target = if (expanded) 1f else 0f
        currentTarget = target
        morphAnim.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    suspend fun animateSecondaryBubble(visible: Boolean) {
        secondaryBubbleAnim.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessLow),
        )
    }

    suspend fun animateEventReplace() {
        replaceAnim.snapTo(0f)
        replaceAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium))
        replaceAnim.snapTo(0f)
    }

    suspend fun animateDismiss() {
        dismissAnim.snapTo(0f)
        dismissAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium))
    }
}

@Composable
fun rememberIslandMotionController(
    expanded: Boolean,
    hasSecondary: Boolean = false,
): IslandMotionController {
    val controller = remember {
        IslandMotionController(
            morphAnim = Animatable(0f),
            secondaryBubbleAnim = Animatable(0f),
            replaceAnim = Animatable(0f),
            dismissAnim = Animatable(0f),
        )
    }
    LaunchedEffect(expanded) { controller.animateMorph(expanded) }
    LaunchedEffect(hasSecondary) { controller.animateSecondaryBubble(hasSecondary) }
    return controller
}
