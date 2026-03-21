package com.dynamicisland.core.cutout

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CutoutType {
    None,
    LeftPunchHole,
    RightPunchHole,
    WideNotch,
    CenterCutout,
}

data class CutoutAnchor(
    val xPx: Float,
    val yPx: Float,
    val cutoutType: CutoutType,
)

class CutoutAnchorProvider {
    private val _anchor = MutableStateFlow(CutoutAnchor(xPx = 0f, yPx = 0f, cutoutType = CutoutType.None))
    val anchor: StateFlow<CutoutAnchor> = _anchor.asStateFlow()

    fun attach(view: View) {
        updateFromInsets(view, view.rootWindowInsets)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            updateFromInsets(v, insets)
            insets
        }
        view.doOnLayout { updateFromInsets(view, view.rootWindowInsets) }
    }

    fun onConfigurationChanged(view: View) {
        updateFromInsets(view, view.rootWindowInsets)
    }

    private fun updateFromInsets(view: View, windowInsets: WindowInsets?) {
        val width = if (view.width > 0) view.width else view.resources.displayMetrics.widthPixels
        val cutoutRects: List<Rect> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowInsets?.displayCutout?.boundingRects ?: emptyList()
        } else {
            emptyList()
        }

        val topRect = cutoutRects.firstOrNull { it.top == 0 }
        val baseX = CutoutAnchorCalculator.calculateTopAnchorX(width, cutoutRects)
        val safeMargin = 56f
        val clampedX = baseX.coerceIn(safeMargin, width - safeMargin)

        val type = when {
            topRect == null -> CutoutType.None
            topRect.width() > width * 0.35f -> CutoutType.WideNotch
            topRect.centerX() < width * 0.33f -> CutoutType.LeftPunchHole
            topRect.centerX() > width * 0.66f -> CutoutType.RightPunchHole
            else -> CutoutType.CenterCutout
        }

        val topInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            windowInsets?.displayCutout?.safeInsetTop ?: 0
        } else 0

        _anchor.value = CutoutAnchor(
            xPx = clampedX,
            yPx = (topInset.coerceAtLeast(1) / 2f),
            cutoutType = type,
        )
    }
}
