package com.example.project_a_android_userapp

import android.content.Context

object LocalStorage {

    private const val PREF_NAME = "zarkit_prefs"

    private const val KEY_PHONE = "phone"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"

    private const val KEY_ACTIVE_RIDE_ID = "active_ride_id"
    private const val KEY_ACTIVE_DRIVER_ID = "active_driver_id" // ✅ REQUIRED

    // ---------------- PHONE ----------------
    fun savePhone(context: Context, phone: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PHONE, phone)
            .apply()
    }

    fun getPhone(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PHONE, null)

    // ---------------- TOKEN ----------------
    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    // ---------------- ROLE ----------------
    fun saveRole(context: Context, role: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getRole(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)

    // ---------------- ACTIVE RIDE ----------------
    fun saveActiveRideId(context: Context, rideId: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_ACTIVE_RIDE_ID, rideId)
            .apply()
    }

    fun getActiveRideId(context: Context): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_ACTIVE_RIDE_ID, -1L)

    // ---------------- ACTIVE DRIVER ----------------
    fun saveActiveDriverId(context: Context, driverId: Long) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_ACTIVE_DRIVER_ID, driverId)
            .apply()
    }

    fun getActiveDriverId(context: Context): Long =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_ACTIVE_DRIVER_ID, -1L)

    // ---------------- CLEAR ----------------
    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
