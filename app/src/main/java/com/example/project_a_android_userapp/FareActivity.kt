package com.example.project_a_android_userapp

import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.math.ceil

class FareActivity : AppCompatActivity() {

    private lateinit var pickupText: TextView
    private lateinit var dropText: TextView
    private lateinit var distanceText: TextView
    private lateinit var timeText: TextView

    private lateinit var bikeCard: CardView
    private lateinit var loaderCard: CardView
    private lateinit var truckCard: CardView

    private lateinit var bikeFareText: TextView
    private lateinit var loaderFareText: TextView
    private lateinit var truckFareText: TextView

    private lateinit var proceedBtn: Button

    private val vm by lazy { (application as MyApp).vm }

    private var selectedVehicle = "Bike"
    private var finalKm = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)
        distanceText = findViewById(R.id.distanceText)
        timeText = findViewById(R.id.timeText)

        bikeCard = findViewById(R.id.vehicleBike)
        loaderCard = findViewById(R.id.vehicleLoader)
        truckCard = findViewById(R.id.vehicleTruck)

        bikeFareText = findViewById(R.id.bikeFareText)
        loaderFareText = findViewById(R.id.loaderFareText)
        truckFareText = findViewById(R.id.truckFareText)

        proceedBtn = findViewById(R.id.proceedBtn)

        // Short Address
        pickupText.text = vm.pickupAddress
        dropText.text = vm.dropAddress

        // Distance
        val km = vm.distanceValue / 1000.0
        finalKm = ceil(km)
        distanceText.text = "Distance: $finalKm km"
        timeText.text = "Time: ${vm.durationText}"

        // Fares
        updateFare(finalKm)

        // Clicks
        bikeCard.setOnClickListener { selectVehicle(bikeCard, "Bike") }
        loaderCard.setOnClickListener { selectVehicle(loaderCard, "Loader") }
        truckCard.setOnClickListener { selectVehicle(truckCard, "Truck") }

        // Default
        selectVehicle(bikeCard, "Bike")

        proceedBtn.setOnClickListener {
            vm.selectedVehicle = selectedVehicle
            vm.finalFare = getFareForVehicle(selectedVehicle, finalKm).toDouble()
            startActivity(Intent(this, FinalFareActivity::class.java))
        }
    }

    private fun selectVehicle(card: CardView, type: String) {
        selectedVehicle = type

        listOf(bikeCard, loaderCard, truckCard).forEach {
            it.setCardBackgroundColor(Color.WHITE)
            it.animate().scaleX(1f).scaleY(1f).duration = 150
        }

        card.setCardBackgroundColor(Color.parseColor("#E0ECFF"))
        card.animate().scaleX(1.05f).scaleY(1.05f).duration = 180

        proceedBtn.text = "Proceed with $type"
    }

    private fun updateFare(km: Double) {
        bikeFareText.text = "₹" + ceil(km * 12)
        loaderFareText.text = "₹" + ceil(km * 20)
        truckFareText.text = "₹" + ceil(km * 40)
    }

    private fun getFareForVehicle(type: String, km: Double): Int {
        return when (type) {
            "Bike" -> ceil(km * 12).toInt()
            "Loader" -> ceil(km * 20).toInt()
            "Truck" -> ceil(km * 40).toInt()
            else -> 0
        }
    }
}
