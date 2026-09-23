package com.mahdi155000.clof_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)
        setContent {
            var darkMode by remember { mutableStateOf(settingsManager.isDarkMode()) }
            MaterialTheme(
                colorScheme = if (darkMode) {
                    androidx.compose.material3.darkColorScheme()
                } else {
                    androidx.compose.material3.lightColorScheme()
                }
            ) {
                ClofApp(
                    darkMode = darkMode,
                    onDarkModeChange = { enabled ->
                        darkMode = enabled
                        settingsManager.setDarkMode(enabled)
                    }
                )
            }
        }
    }
}
