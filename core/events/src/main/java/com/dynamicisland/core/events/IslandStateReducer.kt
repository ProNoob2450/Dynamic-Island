package com.dynamicisland.core.events

object IslandStateReducer {
    fun reduce(current: IslandState, signal: IslandSignal): IslandStateResult {
        return when (signal) {
            is IslandSignal.Event -> onEvent(current, signal.event)
            is IslandSignal.Gesture -> onGesture(current, signal.gesture)
            is IslandSignal.Timeout -> onTimeout(current, signal.timeout)
        }
    }

    private fun onEvent(current: IslandState, event: IslandEvent): IslandStateResult {
        if (event is IslandEvent.Idle) {
            return IslandStateResult(IslandState.Idle, AnimationCommand.ToCompact)
        }

        if (event is IslandEvent.Call) {
            return when (event.callState) {
                CallLifecycleState.Incoming -> IslandStateResult(
                    state = IslandState.IncomingCall(event),
                    animationCommand = AnimationCommand.ToExpanded,
                )

                CallLifecycleState.InCall -> IslandStateResult(
                    state = IslandState.InCall(event),
                    animationCommand = AnimationCommand.ToExpanded,
                )

                CallLifecycleState.Ended -> IslandStateResult(
                    state = IslandState.Dismissed,
                    animationCommand = AnimationCommand.ToDismissed,
                )
            }
        }

        val next = when (current) {
            is IslandState.Pinned -> IslandState.Pinned(event)
            is IslandState.Expanded -> IslandState.Expanded(event)
            else -> IslandState.Peek(event)
        }

        val command = when {
            current is IslandState.Peek && current.event.id != event.id -> AnimationCommand.ReplaceEvent
            current is IslandState.Expanded && current.event.id != event.id -> AnimationCommand.ReplaceEvent
            next is IslandState.Expanded || next is IslandState.Pinned -> AnimationCommand.None
            else -> AnimationCommand.ToCompact
        }

        return IslandStateResult(next, command)
    }

    private fun onGesture(current: IslandState, gesture: UserGesture): IslandStateResult {
        return when (gesture) {
            UserGesture.Tap -> when (current) {
                is IslandState.Peek -> IslandStateResult(IslandState.Expanded(current.event), AnimationCommand.ToExpanded)
                is IslandState.Expanded -> IslandStateResult(IslandState.Peek(current.event), AnimationCommand.ToCompact)
                is IslandState.Pinned -> IslandStateResult(IslandState.Expanded(current.event), AnimationCommand.ToExpanded)
                is IslandState.IncomingCall,
                is IslandState.InCall,
                -> IslandStateResult(current, AnimationCommand.None)
                else -> IslandStateResult(current, AnimationCommand.None)
            }

            UserGesture.LongPress -> when (current) {
                is IslandState.Expanded -> IslandStateResult(IslandState.Pinned(current.event), AnimationCommand.None)
                is IslandState.Peek -> IslandStateResult(IslandState.Pinned(current.event), AnimationCommand.ToExpanded)
                is IslandState.Pinned -> IslandStateResult(IslandState.Peek(current.event), AnimationCommand.ToCompact)
                else -> IslandStateResult(current, AnimationCommand.None)
            }

            UserGesture.SwipeUpDismiss -> IslandStateResult(IslandState.Dismissed, AnimationCommand.ToDismissed)

            UserGesture.SwipeDownPeek -> when (current) {
                is IslandState.Peek -> IslandStateResult(IslandState.Expanded(current.event), AnimationCommand.QuickPeek)
                is IslandState.Idle, IslandState.Dismissed -> IslandStateResult(current, AnimationCommand.None)
                else -> IslandStateResult(current, AnimationCommand.QuickPeek)
            }
        }
    }

    private fun onTimeout(current: IslandState, timeout: TimeoutEvent): IslandStateResult {
        return when (timeout) {
            TimeoutEvent.AutoCollapse -> when (current) {
                is IslandState.Expanded -> IslandStateResult(IslandState.Peek(current.event), AnimationCommand.ToCompact)
                is IslandState.Peek -> IslandStateResult(IslandState.Idle, AnimationCommand.ToCompact)
                else -> IslandStateResult(current, AnimationCommand.None)
            }
        }
    }
}
