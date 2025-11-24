package com.example.project_a_android_userapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.osmdroid.util.GeoPoint
import kotlin.math.*

class FareActivity : AppCompatActivity() {

    private lateinit var pickupText: TextView
    private lateinit var dropText: TextView
    private lateinit var bikeFareText: TextView
    private lateinit var truckFareText: TextView
    private lateinit var bigTruckFareText: TextView

    private lateinit var bikeCard: CardView
    private lateinit var truckCard: CardView
    private lateinit var bigTruckCard: CardView

    private lateinit var submitButton: Button

    private var selectedVehicle: String = "Bike"
    private var selectedFare: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        val pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        val pickupLon = intent.getDoubleExtra("pickupLon", 0.0)
        val dropLat = intent.getDoubleExtra("dropLat", 0.0)
        val dropLon = intent.getDoubleExtra("dropLon", 0.0)

        val distance = calculateDistance(
            GeoPoint(pickupLat, pickupLon),
            GeoPoint(dropLat, dropLon)
        )

        // Calculate fares
        val bikeFare = distance * 10
        val truckFare = distance * 25
        val bigTruckFare = distance * 40

        // Views
        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)
        bikeFareText = findViewById(R.id.bikeFareText)
        truckFareText = findViewById(R.id.truckFareText)
        bigTruckFareText = findViewById(R.id.bigTruckFareText)

        bikeCard = findViewById(R.id.bikeCard)
        truckCard = findViewById(R.id.truckCard)
        bigTruckCard = findViewById(R.id.bigTruckCard)

        submitButton = findViewById(R.id.submitFareButton)

        // Set texts
        pickupText.text = "Pickup: $pickupLat, $pickupLon"
        dropText.text = "Drop: $dropLat, $dropLon"

        bikeFareText.text = "Bike: ₹${String.format("%.2f", bikeFare)}"
        truckFareText.text = "Truck: ₹${String.format("%.2f", truckFare)}"
        bigTruckFareText.text = "Big Truck: ₹${String.format("%.2f", bigTruckFare)}"

        // Vehicle cards + fare lists
        val cards = listOf(bikeCard, truckCard, bigTruckCard)
        val fares = listOf("Bike", "Truck", "Big Truck")
        val fareValues = listOf(bikeFare, truckFare, bigTruckFare)

        for (i in cards.indices) {
            cards[i].setOnClickListener {
                selectedVehicle = fares[i]
                selectedFare = fareValues[i]
                highlightSelectedCard(cards[i])
            }
        }

        // Default selection = Bike
        selectedFare = bikeFare
        highlightSelectedCard(bikeCard)

        // Continue to review booking screen
        submitButton.setOnClickListener {
            val intent = Intent(this, FinalFareActivity::class.java)
            intent.putExtra("pickupLat", pickupLat)
            intent.putExtra("pickupLon", pickupLon)
            intent.putExtra("dropLat", dropLat)
            intent.putExtra("dropLon", dropLon)
            intent.putExtra("vehicle", selectedVehicle)
            intent.putExtra("fare", selectedFare)
            startActivity(intent)
        }
    }

    // Highlight selected vehicle card
    private fun highlightSelectedCard(selectedCard: CardView) {
        val allCards = listOf(bikeCard, truckCard, bigTruckCard)

        for (card in allCards) {
            card.setCardBackgroundColor(Color.WHITE)
            card.scaleX = 1f
            card.scaleY = 1f
        }

        selectedCard.setCardBackgroundColor(Color.parseColor("#FFDD00"))

        // Simple animation when selected
        selectedCard.animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(180)
            .start()
    }

    // Haversine distance calculation
    private fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
        val R = 6371.0 // km
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dLat / 2).pow(2.0) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}
