package com.dynamicisland.core.events

class NavigationEventProvider(
    private val onEvent: (IslandEvent) -> Unit,
) {
    fun publishNavigation(nextInstruction: String, distanceMeters: Int, etaMinutes: Int) {
        onEvent(
            IslandEvent.Navigation(
                nextInstruction = nextInstruction,
                distanceMeters = distanceMeters,
                etaMinutes = etaMinutes,
            )
        )
    }

    fun endNavigation() {
        onEvent(IslandEvent.Idle)
    }
}
