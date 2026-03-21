package com.dynamicisland.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dynamicisland.app.settings.IslandSettingsRepository
import com.dynamicisland.core.events.IslandEvent
import com.dynamicisland.core.events.IslandEventManager
import com.dynamicisland.core.overlay.IslandOverlayService
import com.dynamicisland.core.ui.DynamicIslandHomeScreen
import com.dynamicisland.core.ui.DynamicIslandTheme
import com.dynamicisland.core.ui.IslandSettingsUiState
import com.dynamicisland.core.ui.debugIncomingCallEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && canStartOverlay()) {
            IslandOverlayService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IslandEventManager.initialize(this)

        if (!hasPhonePermission()) {
            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }

        if (canStartOverlay()) {
            IslandOverlayService.start(this)
        }

        setContent {
            val repo = remember { IslandSettingsRepository(applicationContext) }
            val settings by repo.settings.collectAsState(initial = IslandSettingsUiState())

            DynamicIslandTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DynamicIslandHomeScreen(
                        settings = settings,
                        hasOverlayPermission = Settings.canDrawOverlays(this),
                        hasNotificationAccess = hasNotificationListenerAccess(),
                        hasPhonePermission = hasPhonePermission(),
                        onRequestOverlayPermission = { openOverlayPermissionSettings() },
                        onRequestNotificationAccess = { openNotificationListenerSettings() },
                        onRequestPhonePermission = { phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE) },
                        onSettingsChanged = {
                            lifecycleScope.launch { repo.update(it) }
                        },
                        onStartOverlay = { if (canStartOverlay()) IslandOverlayService.start(this) },
                        onTriggerMedia = {
                            if (settings.mediaEnabled) {
                                IslandEventManager.publish(
                                    IslandEvent.Media(
                                        title = "Debug Song",
                                        artist = "Debug Artist",
                                        isPlaying = true,
                                        artwork = null,
                                    )
                                )
                            }
                        },
                        onTriggerNotification = {
                            IslandEventManager.publish(
                                IslandEvent.Notification(
                                    packageName = "com.debug",
                                    title = "Debug notification",
                                    text = "Build completed",
                                )
                            )
                        },
                        onTriggerTimer = {
                            if (settings.timerEnabled) {
                                IslandEventManager.triggerTimer("Pomodoro", durationSeconds = 15 * 60L)
                            }
                        },
                        onTriggerNavigation = {
                            IslandEventManager.triggerNavigation("Turn right on 5th Ave", 120, 6)
                        },
                        onTriggerCall = {
                            if (settings.callEnabled) IslandEventManager.publish(debugIncomingCallEvent())
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (canStartOverlay()) {
            IslandOverlayService.start(this)
        }
    }

    private fun canStartOverlay(): Boolean {
        return Settings.canDrawOverlays(this) && hasNotificationListenerAccess() && hasPhonePermission()
    }

    private fun hasPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationListenerAccess(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled?.contains(packageName) == true
    }

    private fun openOverlayPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        startActivity(intent)
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}
