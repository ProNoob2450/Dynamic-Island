package com.dynamicisland.core.events

import org.junit.Assert.assertEquals
import org.junit.Test

class IslandStateReducerTest {
    @Test
    fun `media event from idle moves to peek`() {
        val media = IslandEvent.Media("Song", "Artist", true, artwork = null)
        val result = IslandStateReducer.reduce(
            current = IslandState.Idle,
            signal = IslandSignal.Event(media),
        )

        assertEquals(IslandState.Peek(media), result.state)
        assertEquals(AnimationCommand.ToCompact, result.animationCommand)
    }

    @Test
    fun `tap on peek expands`() {
        val media = IslandEvent.Media("Song", "Artist", true, artwork = null)
        val current = IslandState.Peek(media)
        val result = IslandStateReducer.reduce(current, IslandSignal.Gesture(UserGesture.Tap))

        assertEquals(IslandState.Expanded(media), result.state)
        assertEquals(AnimationCommand.ToExpanded, result.animationCommand)
    }

    @Test
    fun `swipe up dismisses`() {
        val media = IslandEvent.Media("Song", "Artist", true, artwork = null)
        val current = IslandState.Peek(media)
        val result = IslandStateReducer.reduce(current, IslandSignal.Gesture(UserGesture.SwipeUpDismiss))

        assertEquals(IslandState.Dismissed, result.state)
        assertEquals(AnimationCommand.ToDismissed, result.animationCommand)
    }

    @Test
    fun `call lifecycle transitions idle to incoming then in call then dismissed`() {
        val incoming = IslandEvent.Call(
            callerName = "Alice",
            callerNumber = "+12025550123",
            callState = CallLifecycleState.Incoming,
            callDurationSeconds = 0,
        )
        val inCall = incoming.copy(callState = CallLifecycleState.InCall, callDurationSeconds = 8)
        val ended = incoming.copy(callState = CallLifecycleState.Ended, callDurationSeconds = 12)

        val incomingResult = IslandStateReducer.reduce(IslandState.Idle, IslandSignal.Event(incoming))
        assertEquals(IslandState.IncomingCall(incoming), incomingResult.state)

        val inCallResult = IslandStateReducer.reduce(incomingResult.state, IslandSignal.Event(inCall))
        assertEquals(IslandState.InCall(inCall), inCallResult.state)

        val endedResult = IslandStateReducer.reduce(inCallResult.state, IslandSignal.Event(ended))
        assertEquals(IslandState.Dismissed, endedResult.state)
    }
}
