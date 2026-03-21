package com.dynamicisland.core.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IslandQueueState(
    val primary: IslandEvent = IslandEvent.Idle,
    val secondary: IslandEvent? = null,
)

class IslandEventQueue {
    private val events = linkedMapOf<String, IslandEvent>()
    private val _state = MutableStateFlow(IslandQueueState())
    val state: StateFlow<IslandQueueState> = _state.asStateFlow()

    fun upsert(event: IslandEvent) {
        if (event is IslandEvent.Idle) {
            _state.value = IslandQueueState(IslandEvent.Idle, null)
            events.clear()
            return
        }
        events[event.id] = event
        publish()
    }

    fun dismissPrimary() {
        val primaryId = _state.value.primary.id
        events.remove(primaryId)
        publish()
    }

    private fun publish() {
        if (events.isEmpty()) {
            _state.value = IslandQueueState(IslandEvent.Idle, null)
            return
        }

        val sorted = events.values.sortedByDescending { it.priority.score }
        _state.value = IslandQueueState(
            primary = sorted.first(),
            secondary = sorted.getOrNull(1),
        )
    }
}
