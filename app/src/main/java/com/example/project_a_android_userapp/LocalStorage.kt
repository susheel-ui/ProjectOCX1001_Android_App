package com.example.project_a_android_userapp

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {

    private const val PREF_NAME = "zarkit_prefs"

    private const val KEY_PHONE = "phone"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"

    private const val KEY_ACTIVE_RIDE_ID = "active_ride_id"
    private const val KEY_ACTIVE_DRIVER_ID = "active_driver_id"

    // 🔹 Common prefs method
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ---------------- PHONE ----------------
    fun savePhone(context: Context, phone: String) {
        prefs(context).edit().putString(KEY_PHONE, phone).apply()
    }

    fun getPhone(context: Context): String? =
        prefs(context).getString(KEY_PHONE, null)

    // ---------------- TOKEN ----------------
    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)

    // ---------------- ROLE ----------------
    fun saveRole(context: Context, role: String) {
        prefs(context).edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(context: Context): String? =
        prefs(context).getString(KEY_ROLE, null)

    // ---------------- USER ID ✅ FIXED ----------------
    fun saveUserId(context: Context, userId: Int) {
        prefs(context).edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Int =
        prefs(context).getInt(KEY_USER_ID, -1)

    // ---------------- ACTIVE RIDE ----------------
    fun saveActiveRideId(context: Context, rideId: Long) {
        prefs(context).edit().putLong(KEY_ACTIVE_RIDE_ID, rideId).apply()
    }

    fun getActiveRideId(context: Context): Long =
        prefs(context).getLong(KEY_ACTIVE_RIDE_ID, -1L)

    // ---------------- ACTIVE DRIVER ----------------
    fun saveActiveDriverId(context: Context, driverId: Long) {
        prefs(context).edit().putLong(KEY_ACTIVE_DRIVER_ID, driverId).apply()
    }

    fun getActiveDriverId(context: Context): Long =
        prefs(context).getLong(KEY_ACTIVE_DRIVER_ID, -1L)

    // ---------------- CLEAR ----------------
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
