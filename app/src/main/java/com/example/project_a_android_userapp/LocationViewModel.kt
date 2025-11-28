package com.example.project_a_android_userapp

// Simple plain ViewModel-like holder (not Android ViewModel) used as an application singleton store.
class LocationViewModel {
    // PICKUP
    var pickupLat: Double = 0.0
    var pickupLon: Double = 0.0
    var pickupAddress: String = ""

    // DROP
    var dropLat: Double = 0.0
    var dropLon: Double = 0.0
    var dropAddress: String = ""

    // SENDER
    var senderHouse: String = ""
    var senderName: String = ""
    var senderPhone: String = ""
    var senderType: String = ""

    // RECEIVER
    var receiverHouse: String = ""
    var receiverName: String = ""
    var receiverPhone: String = ""
    var receiverType: String = ""
}
