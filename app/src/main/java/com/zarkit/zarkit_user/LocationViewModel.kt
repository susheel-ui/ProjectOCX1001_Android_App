package com.zarkit.zarkit_user

import com.zarkit.zarkit_user.model.BookAgainResponse

// App level shared data holder
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
    var distanceText: String = "0 km"
    var durationText: String = "0 min"

    // meters & seconds from Google API
    var distanceValue: Double = 0.0
    var durationValue: Double = 0.0

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
    // SET DATA FROM REBOOK API
    // =================================================
    fun setFromRebookResponse(data: BookAgainResponse) {

        // Pickup
        pickupLat = data.pickupLat
        pickupLon = data.pickupLon
        pickupAddress = data.pickupAddress ?: ""

        // Drop
        dropLat = data.dropLat
        dropLon = data.dropLon
        dropAddress = data.dropAddress ?: ""

        // Distance & Duration
        distanceText = data.distanceText ?: "0 km"
        durationText = data.durationText ?: "0 min"
        distanceValue = data.distanceValue ?: 0.0
        durationValue = data.durationValue ?: 0.0

        // Sender
        senderHouse = data.senderHouse ?: ""
        senderName = data.senderName ?: ""
        senderPhone = data.senderPhone ?: ""
        senderType = data.senderType ?: ""

        // Receiver
        receiverHouse = data.receiverHouse ?: ""
        receiverName = data.receiverName ?: ""
        receiverPhone = data.receiverPhone ?: ""
        receiverType = data.receiverType ?: ""
    }

    // =================================================
    // CLEAR DATA
    // =================================================
    fun clear() {

        // Pickup
        pickupLat = 0.0
        pickupLon = 0.0
        pickupAddress = ""

        // Drop
        dropLat = 0.0
        dropLon = 0.0
        dropAddress = ""

        // Distance
        distanceText = "0 km"
        durationText = "0 min"
        distanceValue = 0.0
        durationValue = 0.0

        // Sender
        senderHouse = ""
        senderName = ""
        senderPhone = ""
        senderType = ""

        // Receiver
        receiverHouse = ""
        receiverName = ""
        receiverPhone = ""
        receiverType = ""

        // Fare
        selectedVehicle = ""
        finalFare = 0.0
        goodsType = ""
    }
}
