package com.dynamicisland.core.events

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryEventProvider(
    private val context: Context,
    private val onEvent: (IslandEvent) -> Unit,
) {
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val i = intent ?: return
            val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 0
            val manager = context?.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val millis = manager?.computeChargeTimeRemaining() ?: -1L
            val eta = if (millis > 0) {
                val mins = millis / 60000L
                "${mins}m"
            } else {
                "--"
            }

            if (charging) {
                onEvent(
                    IslandEvent.Battery(
                        batteryPercent = percent,
                        isCharging = true,
                        estimatedTimeToFull = eta,
                    )
                )
            } else {
                onEvent(IslandEvent.Idle)
            }
        }
    }

    fun start() {
        if (registered) return
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registered = true
    }

    fun stop() {
        if (!registered) return
        context.unregisterReceiver(receiver)
        registered = false
    }
}
