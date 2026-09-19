package com.mahdi155000.clof_android

import android.content.Context

class SettingsManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "clof_settings",
        Context.MODE_PRIVATE
    )

    fun isDarkMode(): Boolean {
        return preferences.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.edit()
            .putBoolean("dark_mode", enabled)
            .apply()
    }
}