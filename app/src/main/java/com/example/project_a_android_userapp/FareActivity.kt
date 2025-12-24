package com.example.project_a_android_userapp

import android.content.Intent
import android.graphics.Color
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

    // ===== VEHICLE CARDS =====
    private lateinit var bikeEvCard: CardView
    private lateinit var bikePetrolCard: CardView

    private lateinit var loaderEvCard: CardView
    private lateinit var loaderPetrolCard: CardView
    private lateinit var loaderCngCard: CardView

    private lateinit var truckEvCard: CardView
    private lateinit var truckPetrolCard: CardView
    private lateinit var truckCngCard: CardView

    // ===== FARE TEXTS =====
    private lateinit var bikeEvFare: TextView
    private lateinit var bikePetrolFare: TextView

    private lateinit var loaderEvFare: TextView
    private lateinit var loaderPetrolFare: TextView
    private lateinit var loaderCngFare: TextView

    private lateinit var truckEvFare: TextView
    private lateinit var truckPetrolFare: TextView
    private lateinit var truckCngFare: TextView

    private lateinit var proceedBtn: Button

    private val vm by lazy { (application as MyApp).vm }

    private var selectedVehicle = "TWO_WHEELER_PETROL"
    private var finalKm = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        // ===== LOCATION INFO =====
        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)
        distanceText = findViewById(R.id.distanceText)
        timeText = findViewById(R.id.timeText)

        pickupText.text = vm.pickupAddress
        dropText.text = vm.dropAddress

        finalKm = ceil(vm.distanceValue / 1000.0)
        distanceText.text = "Distance: $finalKm km"
        timeText.text = "Time: ${vm.durationText}"

        // ===== CARDS =====
        bikeEvCard = findViewById(R.id.vehicleBikeEv)
        bikePetrolCard = findViewById(R.id.vehicleBikePetrol)

        loaderEvCard = findViewById(R.id.vehicleLoaderEv)
        loaderPetrolCard = findViewById(R.id.vehicleLoaderPetrol)
        loaderCngCard = findViewById(R.id.vehicleLoaderCng)

        truckEvCard = findViewById(R.id.vehicleTruckEv)
        truckPetrolCard = findViewById(R.id.vehicleTruckPetrol)
        truckCngCard = findViewById(R.id.vehicleTruckCng)

        // ===== FARES =====
        bikeEvFare = findViewById(R.id.bikeEvFareText)
        bikePetrolFare = findViewById(R.id.bikePetrolFareText)

        loaderEvFare = findViewById(R.id.loaderEvFareText)
        loaderPetrolFare = findViewById(R.id.loaderPetrolFareText)
        loaderCngFare = findViewById(R.id.loaderCngFareText)

        truckEvFare = findViewById(R.id.truckEvFareText)
        truckPetrolFare = findViewById(R.id.truckPetrolFareText)
        truckCngFare = findViewById(R.id.truckCngFareText)

        proceedBtn = findViewById(R.id.proceedBtn)

        updateFare(finalKm)

        // ===== CLICK LISTENERS =====
        bikeEvCard.setOnClickListener { selectVehicle(bikeEvCard, "TWO_WHEELER_EV") }
        bikePetrolCard.setOnClickListener { selectVehicle(bikePetrolCard, "TWO_WHEELER_PETROL") }

        loaderEvCard.setOnClickListener { selectVehicle(loaderEvCard, "THREE_WHEELER_EV") }
        loaderPetrolCard.setOnClickListener { selectVehicle(loaderPetrolCard, "THREE_WHEELER_PETROL") }
        loaderCngCard.setOnClickListener { selectVehicle(loaderCngCard, "THREE_WHEELER_CNG") }

        truckEvCard.setOnClickListener { selectVehicle(truckEvCard, "FOUR_WHEELER_EV") }
        truckPetrolCard.setOnClickListener { selectVehicle(truckPetrolCard, "FOUR_WHEELER_PETROL") }
        truckCngCard.setOnClickListener { selectVehicle(truckCngCard, "FOUR_WHEELER_CNG") }

        // DEFAULT SELECTION
        selectVehicle(bikePetrolCard, "TWO_WHEELER_PETROL")

        proceedBtn.setOnClickListener {
            vm.selectedVehicle = selectedVehicle
            vm.finalFare = getFareForVehicle(selectedVehicle, finalKm).toDouble()
            startActivity(Intent(this, FinalFareActivity::class.java))
        }
    }

    private fun selectVehicle(card: CardView, type: String) {
        selectedVehicle = type

        val allCards = listOf(
            bikeEvCard, bikePetrolCard,
            loaderEvCard, loaderPetrolCard, loaderCngCard,
            truckEvCard, truckPetrolCard, truckCngCard
        )

        allCards.forEach {
            it.setCardBackgroundColor(Color.WHITE)
            it.animate().scaleX(1f).scaleY(1f).duration = 150
        }

        card.setCardBackgroundColor(Color.parseColor("#E0ECFF"))
        card.animate().scaleX(1.05f).scaleY(1.05f).duration = 180

        proceedBtn.text = "Proceed with $type"
    }

    private fun updateFare(km: Double) {
        bikeEvFare.text = "₹${ceil(km * 10)}"
        bikePetrolFare.text = "₹${ceil(km * 12)}"

        loaderEvFare.text = "₹${ceil(km * 18)}"
        loaderPetrolFare.text = "₹${ceil(km * 20)}"
        loaderCngFare.text = "₹${ceil(km * 19)}"

        truckEvFare.text = "₹${ceil(km * 35)}"
        truckPetrolFare.text = "₹${ceil(km * 40)}"
        truckCngFare.text = "₹${ceil(km * 38)}"
    }

    private fun getFareForVehicle(type: String, km: Double): Int {
        return when (type) {
            "TWO_WHEELER_EV" -> ceil(km * 10).toInt()
            "TWO_WHEELER_PETROL" -> ceil(km * 12).toInt()

            "THREE_WHEELER_EV" -> ceil(km * 18).toInt()
            "THREE_WHEELER_PETROL" -> ceil(km * 20).toInt()
            "THREE_WHEELER_CNG" -> ceil(km * 19).toInt()

            "FOUR_WHEELER_EV" -> ceil(km * 35).toInt()
            "FOUR_WHEELER_PETROL" -> ceil(km * 40).toInt()
            "FOUR_WHEELER_CNG" -> ceil(km * 38).toInt()

            else -> 0
        }
    }
}
