package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings_pref")

class UserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val HAS_DARK_MODE_PREF = booleanPreferencesKey("has_dark_mode_pref")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val budget = preferences[PreferencesKeys.MONTHLY_BUDGET] ?: 5000000.0
        val hour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 21
        val minute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
        val reminderEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: true
        val hasDarkPref = preferences[PreferencesKeys.HAS_DARK_MODE_PREF] ?: false
        val isDark = if (hasDarkPref) preferences[PreferencesKeys.DARK_MODE_ENABLED] else null

        UserSettings(
            monthlyBudget = budget,
            reminderHour = hour,
            reminderMinute = minute,
            isReminderEnabled = reminderEnabled,
            isDarkMode = isDark
        )
    }

    suspend fun updateBudget(budget: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_BUDGET] = budget
        }
    }

    suspend fun updateReminder(hour: Int, minute: Int, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateDarkMode(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) {
                preferences[PreferencesKeys.HAS_DARK_MODE_PREF] = false
            } else {
                preferences[PreferencesKeys.HAS_DARK_MODE_PREF] = true
                preferences[PreferencesKeys.DARK_MODE_ENABLED] = isDark
            }
        }
    }
}
