package com.example.project_a_android_userapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.project_a_android_userapp.api.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil

class FareActivity : AppCompatActivity() {

    // ================= TEXT =================
    private lateinit var pickupText: TextView
    private lateinit var dropText: TextView

    // ================= VEHICLE CARDS =================
    private lateinit var bikeEvCard: CardView
    private lateinit var bikePetrolCard: CardView
    private lateinit var loaderEvCard: CardView
    private lateinit var loaderPetrolCard: CardView
    private lateinit var loaderCngCard: CardView
    private lateinit var truckEvCard: CardView
    private lateinit var truckPetrolCard: CardView
    private lateinit var truckCngCard: CardView

    // ================= FARE TEXTS =================
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

    private var selectedVehicle = ""
    private var finalKm = 0.0
    private val fareMap = HashMap<String, Double>()

    // 🔥 TRACK SELECTED CARD (PORTER STYLE)
    private var selectedCard: CardView? = null

    // ================= LIFECYCLE =================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        bindViews()
        setLocationInfo()
        setupClickListeners()

        proceedBtn.isEnabled = false
        hideAllCards()
        fetchFareFromApi()
    }

    // ================= VIEW BINDING =================
    private fun bindViews() {

        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)

        bikeEvCard = findViewById(R.id.vehicleBikeEv)
        bikePetrolCard = findViewById(R.id.vehicleBikePetrol)

        loaderEvCard = findViewById(R.id.vehicleLoaderEv)
        loaderPetrolCard = findViewById(R.id.vehicleLoaderPetrol)
        loaderCngCard = findViewById(R.id.vehicleLoaderCng)

        truckEvCard = findViewById(R.id.vehicleTruckEv)
        truckPetrolCard = findViewById(R.id.vehicleTruckPetrol)
        truckCngCard = findViewById(R.id.vehicleTruckCng)

        bikeEvFare = findViewById(R.id.bikeEvFareText)
        bikePetrolFare = findViewById(R.id.bikePetrolFareText)
        loaderEvFare = findViewById(R.id.loaderEvFareText)
        loaderPetrolFare = findViewById(R.id.loaderPetrolFareText)
        loaderCngFare = findViewById(R.id.loaderCngFareText)
        truckEvFare = findViewById(R.id.truckEvFareText)
        truckPetrolFare = findViewById(R.id.truckPetrolFareText)
        truckCngFare = findViewById(R.id.truckCngFareText)

        proceedBtn = findViewById(R.id.proceedBtn)
    }

    // ================= LOCATION INFO =================
    private fun setLocationInfo() {
        pickupText.text = vm.pickupAddress
        dropText.text = vm.dropAddress
        finalKm = ceil(vm.distanceValue / 1000.0)
    }

    // ================= API CALL =================
    private fun fetchFareFromApi() {

        val body = JsonObject().apply {
            addProperty("distanceKm", finalKm)
            addProperty("rain", true)
            addProperty("night", true)
            addProperty("highDemand", true)
        }

        ApiClient.api.calculateFare(body)
            .enqueue(object : Callback<List<JsonObject>> {

                override fun onResponse(
                    call: Call<List<JsonObject>>,
                    response: Response<List<JsonObject>>
                ) {
                    if (!response.isSuccessful) return
                    applyApiResponse(response.body() ?: return)
                }

                override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                    Log.e("FARE_API", "API Failed", t)
                }
            })
    }

    // ================= APPLY RESPONSE =================
    private fun applyApiResponse(list: List<JsonObject>) {

        val cardMap = mapOf(
            "TWO_WHEELER_EV" to Pair(bikeEvCard, bikeEvFare),
            "TWO_WHEELER_PETROL" to Pair(bikePetrolCard, bikePetrolFare),
            "THREE_WHEELER_EV" to Pair(loaderEvCard, loaderEvFare),
            "THREE_WHEELER_PETROL" to Pair(loaderPetrolCard, loaderPetrolFare),
            "THREE_WHEELER_CNG" to Pair(loaderCngCard, loaderCngFare),
            "FOUR_WHEELER_EV" to Pair(truckEvCard, truckEvFare),
            "FOUR_WHEELER_PETROL" to Pair(truckPetrolCard, truckPetrolFare),
            "FOUR_WHEELER_CNG" to Pair(truckCngCard, truckCngFare)
        )

        list.forEach { obj ->
            val vehicle = obj["vehicleInfo"]?.asString ?: return@forEach
            val fare = obj["totalFare"]?.asDouble ?: return@forEach

            fareMap[vehicle] = fare
            cardMap[vehicle]?.let {
                it.first.visibility = View.VISIBLE
                it.second.text = "₹${fare.toInt()}"
            }
        }
    }

    // ================= CLICK LISTENERS =================
    private fun setupClickListeners() {

        bikeEvCard.setOnClickListener { selectVehicle(bikeEvCard, "TWO_WHEELER_EV") }
        bikePetrolCard.setOnClickListener { selectVehicle(bikePetrolCard, "TWO_WHEELER_PETROL") }
        loaderEvCard.setOnClickListener { selectVehicle(loaderEvCard, "THREE_WHEELER_EV") }
        loaderPetrolCard.setOnClickListener { selectVehicle(loaderPetrolCard, "THREE_WHEELER_PETROL") }
        loaderCngCard.setOnClickListener { selectVehicle(loaderCngCard, "THREE_WHEELER_CNG") }
        truckEvCard.setOnClickListener { selectVehicle(truckEvCard, "FOUR_WHEELER_EV") }
        truckPetrolCard.setOnClickListener { selectVehicle(truckPetrolCard, "FOUR_WHEELER_PETROL") }
        truckCngCard.setOnClickListener { selectVehicle(truckCngCard, "FOUR_WHEELER_CNG") }

        proceedBtn.setOnClickListener {
            if (selectedVehicle.isEmpty()) return@setOnClickListener
            vm.selectedVehicle = selectedVehicle
            vm.finalFare = fareMap[selectedVehicle] ?: 0.0
            startActivity(Intent(this, FinalFareActivity::class.java))
        }
    }

    // ================= PORTER STYLE SELECTION =================
    private fun selectVehicle(card: CardView, type: String) {

        selectedCard?.let { resetCard(it) }

        card.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(200)
            .start()

        card.cardElevation = 18f
        card.setCardBackgroundColor(Color.parseColor("#E6F0FF"))

        selectedCard = card
        selectedVehicle = type

        proceedBtn.isEnabled = true
        proceedBtn.text = "Proceed with $type"
    }

    private fun resetCard(card: CardView) {
        card.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()

        card.cardElevation = 4f
        card.setCardBackgroundColor(Color.WHITE)
    }

    // ================= UTIL =================
    private fun hideAllCards() {
        listOf(
            bikeEvCard, bikePetrolCard,
            loaderEvCard, loaderPetrolCard, loaderCngCard,
            truckEvCard, truckPetrolCard, truckCngCard
        ).forEach { it.visibility = View.GONE }
    }
}
