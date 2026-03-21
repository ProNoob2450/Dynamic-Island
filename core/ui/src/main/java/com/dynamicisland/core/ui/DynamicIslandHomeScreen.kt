package com.dynamicisland.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dynamicisland.core.events.CallLifecycleState
import com.dynamicisland.core.events.IslandEvent

@Composable
fun DynamicIslandHomeScreen(
    settings: IslandSettingsUiState,
    hasOverlayPermission: Boolean,
    hasNotificationAccess: Boolean,
    hasPhonePermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRequestPhonePermission: () -> Unit,
    onSettingsChanged: (IslandSettingsUiState) -> Unit,
    onStartOverlay: () -> Unit,
    onTriggerMedia: () -> Unit,
    onTriggerNotification: () -> Unit,
    onTriggerTimer: () -> Unit,
    onTriggerNavigation: () -> Unit,
    onTriggerCall: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(20.dp),
    ) {
        Text("Dynamic Island", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (!hasOverlayPermission) Button(onClick = onRequestOverlayPermission) { Text("Grant Overlay Permission") }
        if (!hasNotificationAccess) Button(onClick = onRequestNotificationAccess) { Text("Grant Notification Access") }
        if (!hasPhonePermission) Button(onClick = onRequestPhonePermission) { Text("Grant Phone Permission") }

        Spacer(Modifier.height(10.dp))
        SettingRow("Enable Island", settings.islandEnabled) { onSettingsChanged(settings.copy(islandEnabled = it)) }
        SettingRow("Enable Call", settings.callEnabled) { onSettingsChanged(settings.copy(callEnabled = it)) }
        SettingRow("Enable Media", settings.mediaEnabled) { onSettingsChanged(settings.copy(mediaEnabled = it)) }
        SettingRow("Enable Battery", settings.batteryEnabled) { onSettingsChanged(settings.copy(batteryEnabled = it)) }
        SettingRow("Enable Timer", settings.timerEnabled) { onSettingsChanged(settings.copy(timerEnabled = it)) }

        Spacer(Modifier.height(12.dp))
        Button(onClick = onStartOverlay, enabled = settings.islandEnabled && hasOverlayPermission && hasNotificationAccess && hasPhonePermission) {
            Text("Start Island Overlay")
        }

        Spacer(Modifier.height(20.dp))
        Text("Debug Events", color = Color(0xFF9FA3AA))
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onTriggerMedia) { Text("Media") }
            Button(onClick = onTriggerNotification) { Text("Notification") }
            Button(onClick = onTriggerTimer) { Text("Timer") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onTriggerNavigation) { Text("Navigation") }
            Button(onClick = onTriggerCall) { Text("Incoming Call") }
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color.White)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

data class IslandSettingsUiState(
    val islandEnabled: Boolean = true,
    val callEnabled: Boolean = true,
    val mediaEnabled: Boolean = true,
    val batteryEnabled: Boolean = true,
    val timerEnabled: Boolean = true,
)

fun IslandSettingsUiState.shouldPublish(event: IslandEvent): Boolean = when (event) {
    is IslandEvent.Call -> callEnabled
    is IslandEvent.Media -> mediaEnabled
    is IslandEvent.Battery -> batteryEnabled
    is IslandEvent.Timer -> timerEnabled
    else -> islandEnabled
}

fun debugIncomingCallEvent(): IslandEvent.Call = IslandEvent.Call(
    callerName = "Debug Caller",
    callerNumber = "+123456789",
    callState = CallLifecycleState.Incoming,
    callDurationSeconds = 0,
)
