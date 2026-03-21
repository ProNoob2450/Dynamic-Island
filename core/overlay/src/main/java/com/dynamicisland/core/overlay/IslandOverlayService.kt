package com.dynamicisland.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dynamicisland.core.cutout.CutoutAnchorProvider
import com.dynamicisland.core.events.AnimationCommand
import com.dynamicisland.core.events.CallLifecycleState
import com.dynamicisland.core.events.IslandControlAction
import com.dynamicisland.core.events.IslandEvent
import com.dynamicisland.core.events.IslandEventManager
import com.dynamicisland.core.events.IslandQueueState
import com.dynamicisland.core.events.IslandSignal
import com.dynamicisland.core.events.IslandState
import com.dynamicisland.core.events.IslandStateReducer
import com.dynamicisland.core.events.TimeoutEvent
import com.dynamicisland.core.events.UserGesture
import com.dynamicisland.core.ui.BatteryCompactPill
import com.dynamicisland.core.ui.BatteryIslandCard
import com.dynamicisland.core.ui.CallCompactPill
import com.dynamicisland.core.ui.CallIslandCard
import com.dynamicisland.core.ui.DynamicIslandTheme
import com.dynamicisland.core.ui.IslandCompactPill
import com.dynamicisland.core.ui.IslandExpandedCard
import com.dynamicisland.core.ui.NavigationCompactPill
import com.dynamicisland.core.ui.NavigationIslandCard
import com.dynamicisland.core.ui.TimerCompactPill
import com.dynamicisland.core.ui.TimerIslandCard
import com.dynamicisland.core.ui.rememberIslandMotionController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class IslandOverlayService : LifecycleService() {
    private val overlayController by lazy { OverlayWindowController(this) }
    private val cutoutAnchorProvider = CutoutAnchorProvider()
    private val stateFlow = MutableStateFlow<IslandState>(IslandState.Idle)
    private val queueStateFlow = MutableStateFlow(IslandQueueState())

    private var overlayRootView: View? = null
    private var autoCollapseJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        IslandEventManager.initialize(this)
        IslandEventManager.startProviders()

        try {
            overlayController.showOverlay { composeView ->
                overlayRootView = composeView
                cutoutAnchorProvider.attach(composeView)
                composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                composeView.setContent {
                    DynamicIslandTheme {
                        IslandOverlayComposable(
                            stateFlow = stateFlow,
                            queueStateFlow = queueStateFlow,
                            onGesture = { dispatchGesture(it) },
                            onControlAction = { IslandEventManager.sendControl(it) },
                        )
                    }
                }
            }
        } catch (_: Throwable) {
            stopSelf()
            return
        }

        lifecycleScope.launch {
            cutoutAnchorProvider.anchor.collectLatest { anchor ->
                overlayController.updateAnchor(
                    xPx = (anchor.xPx - COMPACT_PILL_WIDTH_PX / 2f).roundToInt(),
                    yPx = anchor.yPx.roundToInt(),
                )
            }
        }

        lifecycleScope.launch {
            IslandEventManager.queueState.collectLatest { queueState ->
                queueStateFlow.value = queueState
                reduce(IslandSignal.Event(queueState.primary))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            start(this)
        } catch (_: Throwable) {
            // ignore restart failures
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        overlayRootView?.let { cutoutAnchorProvider.onConfigurationChanged(it) }
    }

    override fun onDestroy() {
        autoCollapseJob?.cancel()
        IslandEventManager.stopProviders()
        overlayController.removeOverlay()
        super.onDestroy()
    }

    private fun dispatchGesture(gesture: UserGesture) {
        reduce(IslandSignal.Gesture(gesture))
        if (gesture is UserGesture.SwipeUpDismiss) {
            val current = queueStateFlow.value.primary
            if (current is IslandEvent.Timer) {
                IslandEventManager.sendControl(IslandControlAction.Timer.Cancel)
            }
            IslandEventManager.dismissPrimary()
        }
    }

    private fun reduce(signal: IslandSignal) {
        val result = IslandStateReducer.reduce(stateFlow.value, signal)
        stateFlow.value = result.state
        handleAnimationCommand(result.animationCommand)
    }

    private fun handleAnimationCommand(command: AnimationCommand) {
        when (command) {
            AnimationCommand.ToExpanded,
            AnimationCommand.QuickPeek,
            AnimationCommand.ReplaceEvent,
            -> {
                autoCollapseJob?.cancel()
                autoCollapseJob = lifecycleScope.launch {
                    delay(3500)
                    reduce(IslandSignal.Timeout(TimeoutEvent.AutoCollapse))
                }
            }
            else -> Unit
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Dynamic Island", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island running")
            .setContentText("Overlay is active")
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "dynamic_island_overlay"
        private const val NOTIFICATION_ID = 1337
        private const val COMPACT_PILL_WIDTH_PX = 160

        fun start(context: Context) {
            val intent = Intent(context, IslandOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }
    }
}

@Composable
private fun IslandOverlayComposable(
    stateFlow: MutableStateFlow<IslandState>,
    queueStateFlow: MutableStateFlow<IslandQueueState>,
    onGesture: (UserGesture) -> Unit,
    onControlAction: (IslandControlAction) -> Unit,
) {
    val state by stateFlow.collectAsState()
    val queueState by queueStateFlow.collectAsState()

    val expanded = state is IslandState.Expanded || state is IslandState.Pinned ||
        state is IslandState.IncomingCall || state is IslandState.InCall
    val motion = rememberIslandMotionController(expanded = expanded, hasSecondary = queueState.secondary != null)
    val gestureModifier = IslandGestureController(onGesture).modifier()

    val width = lerpDp(120.dp, 330.dp, motion.progress)
    val height = lerpDp(36.dp, 110.dp, motion.progress)

    AnimatedVisibility(visible = state !is IslandState.Dismissed, enter = fadeIn(), exit = fadeOut()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = width, height = height)
                    .alpha(1f - (0.35f * motion.dismissProgress))
                    .then(gestureModifier),
            ) {
                when (val current = state) {
                    IslandState.Idle, is IslandState.Peek, IslandState.Dismissed -> {
                        val peek = (current as? IslandState.Peek)?.event
                        when (peek) {
                            is IslandEvent.Call -> CallCompactPill(peek.callerName, Modifier.matchParentSize())
                            is IslandEvent.Timer -> TimerCompactPill(peek.remainingSeconds, Modifier.matchParentSize())
                            is IslandEvent.Navigation -> NavigationCompactPill(peek.distanceMeters, Modifier.matchParentSize())
                            is IslandEvent.Battery -> BatteryCompactPill(peek.batteryPercent, Modifier.matchParentSize())
                            else -> IslandCompactPill(Modifier.matchParentSize())
                        }
                    }
                    is IslandState.Expanded -> renderExpanded(current.event, onGesture, onControlAction)
                    is IslandState.Pinned -> renderExpanded(current.event, onGesture, onControlAction)
                    is IslandState.IncomingCall -> renderCallState(current.call, onControlAction)
                    is IslandState.InCall -> renderCallState(current.call, onControlAction)
                }
            }

            if (queueState.secondary != null) {
                Box(
                    modifier = Modifier
                        .offset(x = (-10).dp)
                        .size(22.dp)
                        .alpha(motion.secondaryBubbleAlpha)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2D)),
                )
            }
        }
    }
}

@Composable
private fun renderExpanded(event: IslandEvent, onGesture: (UserGesture) -> Unit, onControlAction: (IslandControlAction) -> Unit) {
    when (event) {
        is IslandEvent.Media -> IslandExpandedCard(
            title = event.title,
            artist = event.artist,
            isPlaying = event.isPlaying,
            artwork = event.artwork,
            onTap = { onGesture(UserGesture.Tap) },
            onPlayPause = { onControlAction(IslandControlAction.Media.TogglePlayPause) },
            onNext = { onControlAction(IslandControlAction.Media.Next) },
            onPrevious = { onControlAction(IslandControlAction.Media.Previous) },
            modifier = Modifier.matchParentSize(),
        )
        is IslandEvent.Call -> renderCallState(event, onControlAction)
        is IslandEvent.Timer -> TimerIslandCard(event.timerLabel, event.remainingSeconds, event.isRunning, Modifier.matchParentSize())
        is IslandEvent.Navigation -> NavigationIslandCard(event.nextInstruction, event.distanceMeters, event.etaMinutes, Modifier.matchParentSize())
        is IslandEvent.Battery -> BatteryIslandCard(event.batteryPercent, event.isCharging, event.estimatedTimeToFull, Modifier.matchParentSize())
        else -> IslandCompactPill(Modifier.matchParentSize())
    }
}

@Composable
private fun renderCallState(call: IslandEvent.Call, onControlAction: (IslandControlAction) -> Unit) {
    CallIslandCard(
        callerName = call.callerName,
        callerNumber = call.callerNumber,
        durationLabel = formatDuration(call.callDurationSeconds),
        showAnswer = call.callState == CallLifecycleState.Incoming,
        onAnswer = { onControlAction(IslandControlAction.Call.Answer) },
        onEnd = { onControlAction(IslandControlAction.Call.End) },
        modifier = Modifier.matchParentSize(),
    )
}

private fun formatDuration(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)
private fun lerpDp(start: Dp, end: Dp, progress: Float): Dp = (start.value + (end.value - start.value) * progress).dp
