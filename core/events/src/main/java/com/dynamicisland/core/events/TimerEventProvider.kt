package com.dynamicisland.core.events

import android.os.CountDownTimer

class TimerEventProvider(
    private val onEvent: (IslandEvent) -> Unit,
) {
    private var timer: CountDownTimer? = null
    private var label: String = "Timer"

    fun startTimer(timerLabel: String, durationSeconds: Long) {
        cancelTimer()
        label = timerLabel
        timer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                onEvent(
                    IslandEvent.Timer(
                        timerLabel = label,
                        remainingSeconds = (millisUntilFinished / 1000L).coerceAtLeast(0L),
                        isRunning = true,
                    )
                )
            }

            override fun onFinish() {
                onEvent(
                    IslandEvent.Timer(
                        timerLabel = label,
                        remainingSeconds = 0,
                        isRunning = false,
                    )
                )
                onEvent(IslandEvent.Idle)
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
        onEvent(IslandEvent.Idle)
    }
}
