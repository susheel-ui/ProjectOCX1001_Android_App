package com.example.project_a_android_userapp

import android.content.Context

/**
 * Simple helper for storing small app data (phone number) using SharedPreferences.
 * Usage:
 *   LocalStorage.savePhone(context, "+911234567890")
 *   val phone = LocalStorage.getPhone(context)
 *   LocalStorage.clearPhone(context)
 */
object LocalStorage {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_PHONE = "phone_number"

    /**
     * Save phone number (overwrites previous).
     */
    fun savePhone(context: Context, phone: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }

    /**
     * Return phone number or null if not set.
     */
    fun getPhone(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PHONE, null)
    }

    /**
     * Remove stored phone number.
     */
    fun clearPhone(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PHONE).apply()
    }
}
