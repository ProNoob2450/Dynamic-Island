package com.dynamicisland.core.events

import android.graphics.Bitmap
import java.util.UUID

enum class IslandEventPriority(val score: Int) {
    Battery(0),
    Notification(1),
    Media(2),
    Timer(3),
    Navigation(4),
    Call(5),
}

enum class CallLifecycleState {
    Incoming,
    InCall,
    Ended,
}

sealed interface IslandEvent {
    val id: String
    val priority: IslandEventPriority

    data object Idle : IslandEvent {
        override val id: String = "idle"
        override val priority: IslandEventPriority = IslandEventPriority.Battery
    }

    data class Media(
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val artwork: Bitmap?,
        val sourcePackage: String? = null,
        override val id: String = UUID.randomUUID().toString(),
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Media
    }

    data class Notification(
        val packageName: String,
        val title: String,
        val text: String,
        override val id: String = UUID.randomUUID().toString(),
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Notification
    }

    data class Timer(
        val timerLabel: String,
        val remainingSeconds: Long,
        val isRunning: Boolean,
        override val id: String = "timer",
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Timer
    }

    data class Navigation(
        val nextInstruction: String,
        val distanceMeters: Int,
        val etaMinutes: Int,
        override val id: String = "navigation",
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Navigation
    }

    data class Battery(
        val batteryPercent: Int,
        val isCharging: Boolean,
        val estimatedTimeToFull: String,
        override val id: String = "battery",
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Battery
    }

    data class Call(
        val callerName: String,
        val callerNumber: String,
        val callState: CallLifecycleState,
        val callDurationSeconds: Long,
        override val id: String = "call",
    ) : IslandEvent {
        override val priority: IslandEventPriority = IslandEventPriority.Call
    }
}

sealed interface IslandSignal {
    data class Event(val event: IslandEvent) : IslandSignal
    data class Gesture(val gesture: UserGesture) : IslandSignal
    data class Timeout(val timeout: TimeoutEvent) : IslandSignal
}

sealed interface UserGesture {
    data object Tap : UserGesture
    data object LongPress : UserGesture
    data object SwipeUpDismiss : UserGesture
    data object SwipeDownPeek : UserGesture
}

sealed interface TimeoutEvent {
    data object AutoCollapse : TimeoutEvent
}

enum class AnimationCommand {
    None,
    ToCompact,
    ToExpanded,
    ToDismissed,
    QuickPeek,
    ReplaceEvent,
    MergeBubble,
}

sealed interface IslandControlAction {
    sealed interface Media : IslandControlAction {
        data object TogglePlayPause : Media
        data object Next : Media
        data object Previous : Media
    }

    sealed interface Call : IslandControlAction {
        data object Answer : Call
        data object End : Call
    }

    sealed interface Timer : IslandControlAction {
        data object Cancel : Timer
    }
}
