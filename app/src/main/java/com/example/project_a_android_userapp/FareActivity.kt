package com.example.project_a_android_userapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class FareActivity : AppCompatActivity() {

    private lateinit var pickupText: TextView
    private lateinit var dropText: TextView

    private lateinit var bikeCard: CardView
    private lateinit var truckCard: CardView
    private lateinit var bigTruckCard: CardView

    private lateinit var bikeFareText: TextView
    private lateinit var truckFareText: TextView
    private lateinit var bigTruckFareText: TextView

    private lateinit var submitButton: Button

    private var selectedVehicle: String = "Bike"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        // Get pickup / drop strings (optional)
        val pickup = intent.getStringExtra("pickup") ?: "Pickup Location"
        val drop = intent.getStringExtra("drop") ?: "Drop Location"

        // Bind Views
        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)

        bikeCard = findViewById(R.id.bikeCard)
        truckCard = findViewById(R.id.truckCard)
        bigTruckCard = findViewById(R.id.bigTruckCard)

        bikeFareText = findViewById(R.id.bikeFareText)
        truckFareText = findViewById(R.id.truckFareText)
        bigTruckFareText = findViewById(R.id.bigTruckFareText)

        submitButton = findViewById(R.id.submitFareButton)

        // Set texts
        pickupText.text = pickup
        dropText.text = drop

        // Click listeners
        bikeCard.setOnClickListener {
            selectedVehicle = "Bike"
            highlight(bikeCard)
        }
        truckCard.setOnClickListener {
            selectedVehicle = "Truck"
            highlight(truckCard)
        }
        bigTruckCard.setOnClickListener {
            selectedVehicle = "Big Truck"
            highlight(bigTruckCard)
        }

        // Default selection
        highlight(bikeCard)

        // Submit
        submitButton.setOnClickListener {
            val intent = Intent(this, FinalFareActivity::class.java)
            intent.putExtra("vehicle", selectedVehicle)
            startActivity(intent)
        }
    }

    private fun highlight(card: CardView) {
        val all = listOf(bikeCard, truckCard, bigTruckCard)

        all.forEach {
            it.setCardBackgroundColor(Color.WHITE)
            it.scaleX = 1f
            it.scaleY = 1f
        }

        card.setCardBackgroundColor(Color.parseColor("#FFDD00"))
        card.animate().scaleX(1.05f).scaleY(1.05f).duration = 180
    }
}
