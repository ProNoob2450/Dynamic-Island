package com.dynamicisland.core.events

sealed interface IslandState {
    data object Idle : IslandState
    data class Peek(val event: IslandEvent) : IslandState
    data class Expanded(val event: IslandEvent) : IslandState
    data class Pinned(val event: IslandEvent) : IslandState
    data class IncomingCall(val call: IslandEvent.Call) : IslandState
    data class InCall(val call: IslandEvent.Call) : IslandState
    data object Dismissed : IslandState
}

data class IslandStateResult(
    val state: IslandState,
    val animationCommand: AnimationCommand,
)
