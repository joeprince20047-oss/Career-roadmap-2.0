package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
  try {
    val app: FirebaseApp? = if (FirebaseApp.getApps(context).isEmpty()) {
      try {
        FirebaseApp.initializeApp(context)
      } catch (e: Exception) {
        Log.w(TAG, "Default initializeApp(context) failed; attempting with options", e)
        null
      }
    } else {
      try {
        FirebaseApp.getInstance()
      } catch (_: Exception) {
        null
      }
    }

    val activeApp = app ?: run {
      try {
        val options = FirebaseOptions.Builder()
          .setApplicationId(context.packageName)
          .setProjectId("career-roadmap-dev")
          .setApiKey("AIzaSyDevKeyPlaceholderForAppCheck000000")
          .build()
        FirebaseApp.initializeApp(context, options)
      } catch (e: Exception) {
        Log.e(TAG, "Could not initialize fallback FirebaseApp", e)
        null
      }
    }

    if (activeApp == null) {
      Log.w(TAG, "FirebaseApp is not available, skipping App Check.")
      return
    }

    if (BuildConfig.DEBUG) {
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
      if (!debugToken.isNullOrEmpty()) {
        val persistenceKey = activeApp.persistenceKey
        val prefs = context.getSharedPreferences(
          "$DEBUG_STORE_PREFIX$persistenceKey",
          Context.MODE_PRIVATE
        )
        prefs.edit()
          .putString(DEBUG_SECRET_KEY, debugToken)
          .commit()
        Log.d(TAG, "App Check debug secret configured successfully.")
      }

      FirebaseAppCheck.getInstance(activeApp).installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
      Log.d(TAG, "DebugAppCheckProviderFactory installed successfully.")
    } else {
      val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
      if (resId != 0) {
        val siteKey = context.getString(resId)
        if (siteKey.isNotBlank()) {
          FirebaseAppCheck.getInstance(activeApp).installAppCheckProviderFactory(
            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
          )
          Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
        }
      }
    }
  } catch (e: Exception) {
    Log.e(TAG, "Failed to initialize Firebase App Check", e)
  }
}
