package com.dynamicisland.core.cutout

import android.graphics.Rect

/**
 * Basic cutout anchoring policy for v0 scaffold.
 */
object CutoutAnchorCalculator {
    fun calculateTopAnchorX(
        displayWidthPx: Int,
        boundingRects: List<Rect>,
    ): Float {
        val topRect = boundingRects.firstOrNull { it.top == 0 }
        return if (topRect != null) {
            (topRect.left + topRect.right) / 2f
        } else {
            displayWidthPx / 2f
        }
    }
}
