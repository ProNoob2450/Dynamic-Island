package com.dynamicisland.core.events

import android.content.Context
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

object IslandEventManager {
    private val queue = IslandEventQueue()
    private val controlFlow = MutableSharedFlow<IslandControlAction>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var batteryProvider: BatteryEventProvider? = null
    private var timerProvider: TimerEventProvider? = null
    private var navigationProvider: NavigationEventProvider? = null

    val queueState: StateFlow<IslandQueueState> = queue.state

    fun initialize(context: Context) {
        if (batteryProvider == null) {
            batteryProvider = BatteryEventProvider(context.applicationContext) { publish(it) }
        }
        if (timerProvider == null) {
            timerProvider = TimerEventProvider { publish(it) }
        }
        if (navigationProvider == null) {
            navigationProvider = NavigationEventProvider { publish(it) }
        }
    }

    fun startProviders() {
        batteryProvider?.start()
    }

    fun stopProviders() {
        batteryProvider?.stop()
    }

    fun triggerTimer(label: String, durationSeconds: Long) {
        timerProvider?.startTimer(label, durationSeconds)
    }

    fun cancelTimer() {
        timerProvider?.cancelTimer()
    }

    fun triggerNavigation(instruction: String, distanceMeters: Int, etaMinutes: Int) {
        navigationProvider?.publishNavigation(instruction, distanceMeters, etaMinutes)
    }

    fun endNavigation() {
        navigationProvider?.endNavigation()
    }

    fun publish(event: IslandEvent) {
        queue.upsert(event)
    }

    fun dismissPrimary() {
        if (queue.state.value.primary is IslandEvent.Timer) {
            cancelTimer()
        }
        queue.dismissPrimary()
    }

    fun controls(): Flow<IslandControlAction> = controlFlow.asSharedFlow()

    fun sendControl(action: IslandControlAction) {
        if (action is IslandControlAction.Timer.Cancel) {
            cancelTimer()
        }
        controlFlow.tryEmit(action)
    }
}
