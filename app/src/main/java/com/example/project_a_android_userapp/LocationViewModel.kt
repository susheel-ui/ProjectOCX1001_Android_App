package com.example.project_a_android_userapp

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
    // Saved when Directions API is fetched
    // ---------------------------
    var distanceText: String = ""       // Example: "12 km"
    var durationText: String = ""       // Example: "25 mins"
    var distanceValue: Int = 0          // In meters
    var durationValue: Int = 0          // In seconds

    // ---------------------------
    // SENDER DETAILS
    // ---------------------------
    var senderHouse: String = ""
    var senderName: String = ""
    var senderPhone: String = ""
    var senderType: String = ""         // personal / shop / office

    // ---------------------------
    // RECEIVER DETAILS
    // ---------------------------
    var receiverHouse: String = ""
    var receiverName: String = ""
    var receiverPhone: String = ""
    var receiverType: String = ""       // personal / shop / office

    // ---------------------------
    // Fair DETAILS
    // ---------------------------
    var selectedVehicle: String = ""
    var finalFare: Double = 0.0
}
