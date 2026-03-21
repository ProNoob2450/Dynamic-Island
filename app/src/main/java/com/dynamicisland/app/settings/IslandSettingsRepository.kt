package com.dynamicisland.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.dynamicisland.core.ui.IslandSettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "island_settings")

class IslandSettingsRepository(private val context: Context) {
    val settings: Flow<IslandSettingsUiState> = context.dataStore.data.map { prefs ->
        IslandSettingsUiState(
            islandEnabled = prefs[Keys.ISLAND_ENABLED] ?: true,
            callEnabled = prefs[Keys.CALL_ENABLED] ?: true,
            mediaEnabled = prefs[Keys.MEDIA_ENABLED] ?: true,
            batteryEnabled = prefs[Keys.BATTERY_ENABLED] ?: true,
            timerEnabled = prefs[Keys.TIMER_ENABLED] ?: true,
        )
    }

    suspend fun update(newState: IslandSettingsUiState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ISLAND_ENABLED] = newState.islandEnabled
            prefs[Keys.CALL_ENABLED] = newState.callEnabled
            prefs[Keys.MEDIA_ENABLED] = newState.mediaEnabled
            prefs[Keys.BATTERY_ENABLED] = newState.batteryEnabled
            prefs[Keys.TIMER_ENABLED] = newState.timerEnabled
        }
    }

    private object Keys {
        val ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        val CALL_ENABLED = booleanPreferencesKey("call_enabled")
        val MEDIA_ENABLED = booleanPreferencesKey("media_enabled")
        val BATTERY_ENABLED = booleanPreferencesKey("battery_enabled")
        val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
    }
}
