package com.dynamicisland.core.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView

class OverlayWindowController(
    private val context: Context,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    private var overlayView: ComposeView? = null

    fun showOverlay(content: (ComposeView) -> Unit) {
        if (overlayView != null) return
        val composeView = ComposeView(context)
        content(composeView)
        windowManager.addView(composeView, layoutParams)
        overlayView = composeView
    }

    fun updateAnchor(xPx: Int, yPx: Int) {
        layoutParams.x = xPx
        layoutParams.y = yPx
        overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
    }

    fun update(config: IslandOverlayConfig) {
        overlayView?.visibility = if (config.isEnabled) View.VISIBLE else View.GONE
    }

    fun removeOverlay() {
        overlayView?.let { view ->
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        }
        overlayView = null
    }
}
