package com.dynamicisland.core.events

import org.junit.Assert.assertTrue
import org.junit.Test

class IslandEventQueueTest {
    @Test
    fun `call event should preempt media due higher priority`() {
        val queue = IslandEventQueue()
        queue.upsert(IslandEvent.Media("Song", "Artist", true, null, id = "media"))
        queue.upsert(
            IslandEvent.Call(
                callerName = "Alice",
                callerNumber = "123",
                callState = CallLifecycleState.Incoming,
                callDurationSeconds = 0,
                id = "call",
            )
        )

        assertTrue(queue.state.value.primary is IslandEvent.Call)
    }
}
