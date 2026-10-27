package com.example.ui.theme

import android.content.Context

enum class ThemeMode(val title: String, val subtitle: String) {
  SYSTEM("System Default", "Follow device light/dark appearance"),
  LIGHT("Light Mode", "Crisp daylight theme with clear contrast"),
  DARK("Dark Mode", "Deep slate dark theme easy on eyes")
}

object ThemePreferences {
  private const val PREFS_NAME = "career_roadmap_theme_prefs"
  private const val KEY_THEME_MODE = "key_theme_mode"

  fun getThemeMode(context: Context): ThemeMode {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
    return try {
      ThemeMode.valueOf(modeName)
    } catch (e: Exception) {
      ThemeMode.SYSTEM
    }
  }

  fun setThemeMode(context: Context, mode: ThemeMode) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
  }
}
