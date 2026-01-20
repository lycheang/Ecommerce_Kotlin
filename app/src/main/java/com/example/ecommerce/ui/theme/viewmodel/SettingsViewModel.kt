package com.example.ecommerce.ui.theme.viewmodel

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val prefs: SharedPreferences) : ViewModel() {

    fun toggleDarkMode(isEnabled: Boolean) {

        prefs.edit().putBoolean("dark_mode", isEnabled).apply()

        // Apply the theme change immediately
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Helper to get current state for UI switches
    fun isDarkModeEnabled(): Boolean = prefs.getBoolean("dark_mode", false)
}