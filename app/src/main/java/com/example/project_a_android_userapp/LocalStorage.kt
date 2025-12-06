package com.example.project_a_android_userapp

import android.content.Context

object LocalStorage {

    private const val PREF_NAME = "zarkit_prefs"
    private const val KEY_PHONE = "phone"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"

    fun savePhone(context: Context, phone: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PHONE, phone)
            .apply()
    }

    fun getPhone(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PHONE, null)
    }

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    fun saveRole(context: Context, role: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getRole(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
