package com.zarkit.zarkit_user

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {

    private const val PREF_NAME = "zarkit_prefs"

    // Auth
    private const val KEY_PHONE = "phone"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"

    // Active Ride
    private const val KEY_ACTIVE_RIDE_ID = "active_ride_id"
    private const val KEY_ACTIVE_DRIVER_ID = "active_driver_id"

    // Pickup Location
    private const val KEY_PICKUP_LAT = "pickup_lat"
    private const val KEY_PICKUP_LNG = "pickup_lng"
    private const val KEY_PICKUP_ADDRESS = "pickup_address"

    // Drop Location
    private const val KEY_DROP_LAT = "drop_lat"
    private const val KEY_DROP_LNG = "drop_lng"
    private const val KEY_DROP_ADDRESS = "drop_address"

    // Distance & Duration
    private const val KEY_DISTANCE_TEXT = "distance_text"
    private const val KEY_DURATION_TEXT = "duration_text"
    private const val KEY_DISTANCE_VALUE = "distance_value"
    private const val KEY_DURATION_VALUE = "duration_value"

    // Sender Details
    private const val KEY_SENDER_HOUSE = "sender_house"
    private const val KEY_SENDER_NAME = "sender_name"
    private const val KEY_SENDER_PHONE = "sender_phone"
    private const val KEY_SENDER_TYPE = "sender_type"

    // Receiver Details
    private const val KEY_RECEIVER_HOUSE = "receiver_house"
    private const val KEY_RECEIVER_NAME = "receiver_name"
    private const val KEY_RECEIVER_PHONE = "receiver_phone"
    private const val KEY_RECEIVER_TYPE = "receiver_type"

    // Fare Details
    private const val KEY_SELECTED_VEHICLE = "selected_vehicle"
    private const val KEY_FINAL_FARE = "final_fare"
    private const val KEY_GOODS_TYPE = "goods_type"


    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    // ─────────────────────────────────────────────────────────────
    // PHONE
    // ─────────────────────────────────────────────────────────────

    fun savePhone(context: Context, phone: String) =
        prefs(context).edit().putString(KEY_PHONE, phone).apply()

    fun getPhone(context: Context): String? =
        prefs(context).getString(KEY_PHONE, null)


    // ─────────────────────────────────────────────────────────────
    // TOKEN
    // ─────────────────────────────────────────────────────────────

    fun saveToken(context: Context, token: String) =
        prefs(context).edit().putString(KEY_TOKEN, token).apply()

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)


    // ─────────────────────────────────────────────────────────────
    // ROLE
    // ─────────────────────────────────────────────────────────────

    fun saveRole(context: Context, role: String) =
        prefs(context).edit().putString(KEY_ROLE, role).apply()

    fun getRole(context: Context): String? =
        prefs(context).getString(KEY_ROLE, null)


    // ─────────────────────────────────────────────────────────────
    // USER ID
    // ─────────────────────────────────────────────────────────────

    fun saveUserId(context: Context, userId: Int) =
        prefs(context).edit().putInt(KEY_USER_ID, userId).apply()

    fun getUserId(context: Context): Int =
        prefs(context).getInt(KEY_USER_ID, -1)


    // ─────────────────────────────────────────────────────────────
    // ACTIVE RIDE
    // ─────────────────────────────────────────────────────────────

    fun saveActiveRideId(context: Context, rideId: Long) =
        prefs(context).edit().putLong(KEY_ACTIVE_RIDE_ID, rideId).apply()

    fun getActiveRideId(context: Context): Long =
        prefs(context).getLong(KEY_ACTIVE_RIDE_ID, -1L)


    // ─────────────────────────────────────────────────────────────
    // ACTIVE DRIVER
    // ─────────────────────────────────────────────────────────────

    fun saveActiveDriverId(context: Context, driverId: Long) =
        prefs(context).edit().putLong(KEY_ACTIVE_DRIVER_ID, driverId).apply()

    fun getActiveDriverId(context: Context): Long =
        prefs(context).getLong(KEY_ACTIVE_DRIVER_ID, -1L)


    // ─────────────────────────────────────────────────────────────
    // PICKUP LOCATION
    // ─────────────────────────────────────────────────────────────

    fun savePickupLocation(context: Context, lat: Double, lng: Double, address: String = "") {
        prefs(context).edit()
            .putFloat(KEY_PICKUP_LAT, lat.toFloat())
            .putFloat(KEY_PICKUP_LNG, lng.toFloat())
            .putString(KEY_PICKUP_ADDRESS, address)
            .apply()
    }

    fun getPickupLat(context: Context): Double =
        prefs(context).getFloat(KEY_PICKUP_LAT, 0f).toDouble()

    fun getPickupLng(context: Context): Double =
        prefs(context).getFloat(KEY_PICKUP_LNG, 0f).toDouble()

    fun getPickupAddress(context: Context): String =
        prefs(context).getString(KEY_PICKUP_ADDRESS, "") ?: ""


    // ─────────────────────────────────────────────────────────────
    // DROP LOCATION
    // ─────────────────────────────────────────────────────────────

    fun saveDropLocation(context: Context, lat: Double, lng: Double, address: String = "") {
        prefs(context).edit()
            .putFloat(KEY_DROP_LAT, lat.toFloat())
            .putFloat(KEY_DROP_LNG, lng.toFloat())
            .putString(KEY_DROP_ADDRESS, address)
            .apply()
    }

    fun getDropLat(context: Context): Double =
        prefs(context).getFloat(KEY_DROP_LAT, 0f).toDouble()

    fun getDropLng(context: Context): Double =
        prefs(context).getFloat(KEY_DROP_LNG, 0f).toDouble()

    fun getDropAddress(context: Context): String =
        prefs(context).getString(KEY_DROP_ADDRESS, "") ?: ""


    // ─────────────────────────────────────────────────────────────
    // DISTANCE & DURATION
    // ─────────────────────────────────────────────────────────────

    fun saveDistanceAndDuration(
        context: Context,
        distanceText: String,
        durationText: String,
        distanceValue: Double,
        durationValue: Double
    ) {
        prefs(context).edit()
            .putString(KEY_DISTANCE_TEXT, distanceText)
            .putString(KEY_DURATION_TEXT, durationText)
            .putFloat(KEY_DISTANCE_VALUE, distanceValue.toFloat())
            .putFloat(KEY_DURATION_VALUE, durationValue.toFloat())
            .apply()
    }

    fun getDistanceText(context: Context): String =
        prefs(context).getString(KEY_DISTANCE_TEXT, "0 km") ?: "0 km"

    fun getDurationText(context: Context): String =
        prefs(context).getString(KEY_DURATION_TEXT, "0 min") ?: "0 min"

    fun getDistanceValue(context: Context): Double =
        prefs(context).getFloat(KEY_DISTANCE_VALUE, 0f).toDouble()

    fun getDurationValue(context: Context): Double =
        prefs(context).getFloat(KEY_DURATION_VALUE, 0f).toDouble()


    // ─────────────────────────────────────────────────────────────
    // SENDER DETAILS
    // ─────────────────────────────────────────────────────────────

    fun saveSenderDetails(
        context: Context,
        house: String,
        name: String,
        phone: String,
        type: String
    ) {
        prefs(context).edit()
            .putString(KEY_SENDER_HOUSE, house)
            .putString(KEY_SENDER_NAME, name)
            .putString(KEY_SENDER_PHONE, phone)
            .putString(KEY_SENDER_TYPE, type)
            .apply()
    }

    fun getSenderHouse(context: Context): String =
        prefs(context).getString(KEY_SENDER_HOUSE, "") ?: ""

    fun getSenderName(context: Context): String =
        prefs(context).getString(KEY_SENDER_NAME, "") ?: ""

    fun getSenderPhone(context: Context): String =
        prefs(context).getString(KEY_SENDER_PHONE, "") ?: ""

    fun getSenderType(context: Context): String =
        prefs(context).getString(KEY_SENDER_TYPE, "") ?: ""


    // ─────────────────────────────────────────────────────────────
    // RECEIVER DETAILS
    // ─────────────────────────────────────────────────────────────

    fun saveReceiverDetails(
        context: Context,
        house: String,
        name: String,
        phone: String,
        type: String
    ) {
        prefs(context).edit()
            .putString(KEY_RECEIVER_HOUSE, house)
            .putString(KEY_RECEIVER_NAME, name)
            .putString(KEY_RECEIVER_PHONE, phone)
            .putString(KEY_RECEIVER_TYPE, type)
            .apply()
    }

    fun getReceiverHouse(context: Context): String =
        prefs(context).getString(KEY_RECEIVER_HOUSE, "") ?: ""

    fun getReceiverName(context: Context): String =
        prefs(context).getString(KEY_RECEIVER_NAME, "") ?: ""

    fun getReceiverPhone(context: Context): String =
        prefs(context).getString(KEY_RECEIVER_PHONE, "") ?: ""

    fun getReceiverType(context: Context): String =
        prefs(context).getString(KEY_RECEIVER_TYPE, "") ?: ""


    // ─────────────────────────────────────────────────────────────
    // FARE DETAILS
    // ─────────────────────────────────────────────────────────────

    fun saveFareDetails(context: Context, vehicle: String, fare: Double, goodsType: String) {
        prefs(context).edit()
            .putString(KEY_SELECTED_VEHICLE, vehicle)
            .putFloat(KEY_FINAL_FARE, fare.toFloat())
            .putString(KEY_GOODS_TYPE, goodsType)
            .apply()
    }

    fun getSelectedVehicle(context: Context): String =
        prefs(context).getString(KEY_SELECTED_VEHICLE, "") ?: ""

    fun getFinalFare(context: Context): Double =
        prefs(context).getFloat(KEY_FINAL_FARE, 0f).toDouble()

    fun getGoodsType(context: Context): String =
        prefs(context).getString(KEY_GOODS_TYPE, "") ?: ""


    // ─────────────────────────────────────────────────────────────
    // CLEAR ACTIVE RIDE DATA  (locations + ride + driver)
    // ─────────────────────────────────────────────────────────────

    fun clearActiveRide(context: Context) {
        prefs(context).edit()
            .remove(KEY_ACTIVE_RIDE_ID)
            .remove(KEY_ACTIVE_DRIVER_ID)
            .remove(KEY_PICKUP_LAT)
            .remove(KEY_PICKUP_LNG)
            .remove(KEY_PICKUP_ADDRESS)
            .remove(KEY_DROP_LAT)
            .remove(KEY_DROP_LNG)
            .remove(KEY_DROP_ADDRESS)
            .apply()
    }


    // ─────────────────────────────────────────────────────────────
    // CLEAR BOOKING SESSION  (everything set during booking flow)
    // ─────────────────────────────────────────────────────────────

    fun clearBookingSession(context: Context) {
        prefs(context).edit()
            .remove(KEY_PICKUP_LAT).remove(KEY_PICKUP_LNG).remove(KEY_PICKUP_ADDRESS)
            .remove(KEY_DROP_LAT).remove(KEY_DROP_LNG).remove(KEY_DROP_ADDRESS)
            .remove(KEY_DISTANCE_TEXT).remove(KEY_DURATION_TEXT)
            .remove(KEY_DISTANCE_VALUE).remove(KEY_DURATION_VALUE)
            .remove(KEY_SENDER_HOUSE).remove(KEY_SENDER_NAME)
            .remove(KEY_SENDER_PHONE).remove(KEY_SENDER_TYPE)
            .remove(KEY_RECEIVER_HOUSE).remove(KEY_RECEIVER_NAME)
            .remove(KEY_RECEIVER_PHONE).remove(KEY_RECEIVER_TYPE)
            .remove(KEY_SELECTED_VEHICLE).remove(KEY_FINAL_FARE).remove(KEY_GOODS_TYPE)
            .apply()
    }


    // ─────────────────────────────────────────────────────────────
    // CLEAR ALL  (logout)
    // ─────────────────────────────────────────────────────────────

    fun clear(context: Context) =
        prefs(context).edit().clear().apply()
}