package com.dynamicisland.app

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.dynamicisland.core.events.CallLifecycleState
import com.dynamicisland.core.events.IslandControlAction
import com.dynamicisland.core.events.IslandEvent
import com.dynamicisland.core.events.IslandEventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IslandNotificationListenerService : NotificationListenerService() {
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var telecomManager: TelecomManager

    private val mediaControllers = mutableMapOf<String, MediaController>()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var lastIncomingNumber: String = "Unknown"
    private var activeCallNumber: String = "Unknown"
    private var callStartMs: Long = 0L
    private var callDurationJob: kotlinx.coroutines.Job? = null

    private val activeSessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        registerActiveControllers(controllers.orEmpty())
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushFromControllers()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushFromControllers()
        }
    }

    @Suppress("DEPRECATION")
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (!phoneNumber.isNullOrBlank()) {
                lastIncomingNumber = phoneNumber
            }
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    val number = phoneNumber ?: lastIncomingNumber
                    activeCallNumber = number
                    publishCallEvent(CallLifecycleState.Incoming, number, durationSeconds = 0)
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (callStartMs == 0L) {
                        callStartMs = System.currentTimeMillis()
                    }
                    val number = if (activeCallNumber.isNotBlank()) activeCallNumber else lastIncomingNumber
                    publishCallEvent(CallLifecycleState.InCall, number, durationSeconds = elapsedDurationSeconds())
                    startCallDurationTicker()
                }

                TelephonyManager.CALL_STATE_IDLE -> {
                    stopCallDurationTicker()
                    if (activeCallNumber.isNotBlank() && activeCallNumber != "Unknown") {
                        publishCallEvent(CallLifecycleState.Ended, activeCallNumber, durationSeconds = elapsedDurationSeconds())
                        serviceScope.launch {
                            kotlinx.coroutines.delay(1200)
                            IslandEventManager.dismissPrimary()
                        }
                    }
                    callStartMs = 0L
                    activeCallNumber = "Unknown"
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            mediaSessionManager = getSystemService(MediaSessionManager::class.java)
            telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        } catch (_: Throwable) {
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionListener,
                ComponentName(this, IslandNotificationListenerService::class.java),
            )

            val currentControllers = mediaSessionManager.getActiveSessions(
                ComponentName(this, IslandNotificationListenerService::class.java),
            )
            registerActiveControllers(currentControllers.orEmpty())
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (_: Throwable) {
            // gracefully ignore on unsupported devices
        }

        serviceScope.launch {
            IslandEventManager.controls().collectLatest { action ->
                when (action) {
                    is IslandControlAction.Media -> handleMediaControl(action)
                    is IslandControlAction.Call -> handleCallControl(action)
                    is IslandControlAction.Timer -> Unit
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onListenerDisconnected() {
        unregisterControllers()
        try { mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionListener) } catch (_: Throwable) {}
        try { telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE) } catch (_: Throwable) {}
        stopCallDurationTicker()
        serviceScope.cancel()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras: Bundle = sbn.notification.extras ?: Bundle.EMPTY
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()

        if (title.isNotBlank() || text.isNotBlank()) {
            IslandEventManager.publish(
                IslandEvent.Notification(
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                )
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName in mediaControllers.keys) {
            pushFromControllers()
        }
    }

    private fun handleMediaControl(action: IslandControlAction.Media) {
        val active = getActiveController() ?: return
        when (action) {
            IslandControlAction.Media.TogglePlayPause -> {
                val isPlaying = active.playbackState?.state == PlaybackState.STATE_PLAYING
                if (isPlaying) active.transportControls.pause() else active.transportControls.play()
            }
            IslandControlAction.Media.Next -> active.transportControls.skipToNext()
            IslandControlAction.Media.Previous -> active.transportControls.skipToPrevious()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleCallControl(action: IslandControlAction.Call) {
        try {
            when (action) {
                IslandControlAction.Call.Answer -> telecomManager.acceptRingingCall()
                IslandControlAction.Call.End -> telecomManager.endCall()
            }
        } catch (_: SecurityException) {
            // Permissions/user role restrictions are handled by platform; ignore gracefully.
        }
    }

    private fun publishCallEvent(state: CallLifecycleState, number: String, durationSeconds: Long) {
        IslandEventManager.publish(
            IslandEvent.Call(
                callerName = formatCallerName(number),
                callerNumber = number,
                callState = state,
                callDurationSeconds = durationSeconds,
            )
        )
    }

    private fun startCallDurationTicker() {
        stopCallDurationTicker()
        callDurationJob = serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                publishCallEvent(
                    state = CallLifecycleState.InCall,
                    number = activeCallNumber,
                    durationSeconds = elapsedDurationSeconds(),
                )
            }
        }
    }

    private fun stopCallDurationTicker() {
        callDurationJob?.cancel()
        callDurationJob = null
    }

    private fun elapsedDurationSeconds(): Long {
        if (callStartMs <= 0L) return 0L
        return ((System.currentTimeMillis() - callStartMs) / 1000L).coerceAtLeast(0L)
    }

    private fun registerActiveControllers(controllers: List<MediaController>) {
        val newPackages = controllers.map { it.packageName }.toSet()

        val toRemove = mediaControllers.keys.filterNot { it in newPackages }
        toRemove.forEach { pkg ->
            mediaControllers.remove(pkg)?.unregisterCallback(mediaControllerCallback)
        }

        controllers.forEach { controller ->
            if (mediaControllers.put(controller.packageName, controller) == null) {
                controller.registerCallback(mediaControllerCallback)
            }
        }
        pushFromControllers()
    }

    private fun unregisterControllers() {
        mediaControllers.values.forEach { it.unregisterCallback(mediaControllerCallback) }
        mediaControllers.clear()
    }

    private fun getActiveController(): MediaController? {
        return mediaControllers.values.firstOrNull { controller ->
            controller.playbackState?.state == PlaybackState.STATE_PLAYING ||
                controller.playbackState?.state == PlaybackState.STATE_PAUSED
        }
    }

    private fun pushFromControllers() {
        val active = getActiveController()

        if (active == null) {
            IslandEventManager.publish(IslandEvent.Idle)
            return
        }

        val metadata = active.metadata
        val isPlaying = active.playbackState?.state == PlaybackState.STATE_PLAYING
        IslandEventManager.publish(
            IslandEvent.Media(
                title = metadata.stringOrDefault(MediaMetadata.METADATA_KEY_TITLE, "Unknown title"),
                artist = metadata.stringOrDefault(MediaMetadata.METADATA_KEY_ARTIST, "Unknown artist"),
                isPlaying = isPlaying,
                artwork = metadata.extractArtwork(),
                sourcePackage = active.packageName,
                id = "${active.packageName}:media",
            )
        )
    }

    private fun formatCallerName(number: String): String {
        return if (number.isBlank() || number == "Unknown") "Incoming call" else number
    }
}

private fun MediaMetadata?.stringOrDefault(key: String, default: String): String {
    return this?.getString(key)?.takeIf { it.isNotBlank() } ?: default
}

private fun MediaMetadata?.extractArtwork(): Bitmap? {
    if (this == null) return null
    return getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        ?: getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
}
