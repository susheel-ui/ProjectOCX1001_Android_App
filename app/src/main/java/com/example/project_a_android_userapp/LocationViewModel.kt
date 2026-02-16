package com.example.project_a_android_userapp

import com.example.project_a_android_userapp.model.BookAgainResponse

// Simple plain ViewModel-like holder (app-level shared data)
class LocationViewModel {

    // ---------------------------
    // PICKUP
    // ---------------------------
    var pickupLat: Double = 0.0
    var pickupLon: Double = 0.0
    var pickupAddress: String = ""

    // ---------------------------
    // DROP
    // ---------------------------
    var dropLat: Double = 0.0
    var dropLon: Double = 0.0
    var dropAddress: String = ""

    // ---------------------------
    // DISTANCE & TIME
    // ---------------------------
    var distanceText: String = ""
    var durationText: String = ""
    var distanceValue: Int = 0
    var durationValue: Int = 0

    // ---------------------------
    // SENDER DETAILS
    // ---------------------------
    var senderHouse: String = ""
    var senderName: String = ""
    var senderPhone: String = ""
    var senderType: String = ""

    // ---------------------------
    // RECEIVER DETAILS
    // ---------------------------
    var receiverHouse: String = ""
    var receiverName: String = ""
    var receiverPhone: String = ""
    var receiverType: String = ""

    // ---------------------------
    // FARE DETAILS
    // ---------------------------
    var selectedVehicle: String = ""
    var finalFare: Double = 0.0

    var goodsType: String = ""

    // =================================================
    //  NEW: SET DATA FROM REBOOK API
    // =================================================
    fun setFromRebookResponse(data: BookAgainResponse) {

        // Pickup
        pickupLat = data.pickupLat
        pickupLon = data.pickupLon
        pickupAddress = data.pickupAddress

        // Drop
        dropLat = data.dropLat
        dropLon = data.dropLon
        dropAddress = data.dropAddress

        // Distance & Duration
        distanceText = data.distanceText
        durationText = data.durationText
        distanceValue = data.distanceValue
        durationValue = data.durationValue

        // Sender
        senderHouse = data.senderHouse
        senderName = data.senderName
        senderPhone = data.senderPhone
        senderType = data.senderType

        // Receiver
        receiverHouse = data.receiverHouse
        receiverName = data.receiverName
        receiverPhone = data.receiverPhone
        receiverType = data.receiverType
    }

    // =================================================
    // OPTIONAL: CLEAR DATA (use when new booking starts)
    // =================================================
    fun clear() {
        pickupLat = 0.0
        pickupLon = 0.0
        pickupAddress = ""

        dropLat = 0.0
        dropLon = 0.0
        dropAddress = ""

        distanceText = ""
        durationText = ""
        distanceValue = 0
        durationValue = 0

        senderHouse = ""
        senderName = ""
        senderPhone = ""
        senderType = ""

        receiverHouse = ""
        receiverName = ""
        receiverPhone = ""
        receiverType = ""

        selectedVehicle = ""
        finalFare = 0.0
        goodsType = ""
    }
}
