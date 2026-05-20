package com.zarkit.zarkit_user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zarkit.zarkit_user.api.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FareActivity : BaseActivity() {

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

    private var selectedCard: CardView? = null

    // ================= LIFECYCLE =================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fare)
        // ✅ HANDLE SYSTEM BARS (STATUS + NAV BAR)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


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

        finalKm = when {
            vm.distanceValue > 0 -> {
                vm.distanceValue / 1000.0
            }

            vm.distanceText.isNotEmpty() -> {
                extractKmFromText(vm.distanceText)
            }

            else -> 0.0
        }

        Log.d("FARE_DEBUG", "Distance KM = $finalKm")
    }


    private fun extractKmFromText(text: String): Double {
        return try {
            text.replace("km", "", true).trim().toDouble()
        } catch (e: Exception) {
            1.0
        }
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

        // ✅ SORT AVAILABLE FIRST
        val sortedList = list.sortedByDescending {
            it["availability"]?.asBoolean ?: false
        }

        sortedList.forEach { obj ->

            val vehicle = obj["vehicleInfo"]?.asString ?: return@forEach
            val fare = obj["totalFare"]?.asDouble ?: 0.0
            val availability = obj["availability"]?.asBoolean ?: false

            cardMap[vehicle]?.let { pair ->

                val card = pair.first
                val fareText = pair.second

                // ✅ REMOVE CARD FROM OLD POSITION
                val parent = card.parent as LinearLayout
                parent.removeView(card)

                // ✅ ADD AGAIN -> AVAILABLE WILL COME FIRST
                parent.addView(card)

                card.visibility = View.VISIBLE

                if (availability) {

                    fareMap[vehicle] = fare

                    fareText.text = "₹${fare.toInt()}"
                    fareText.setTextColor(Color.BLACK)

                    card.alpha = 1f
                    card.isEnabled = true
                    card.isClickable = true
                    card.setCardBackgroundColor(Color.WHITE)

                } else {

                    fareMap.remove(vehicle)

                    fareText.text = "Not Available"
                    fareText.setTextColor(Color.BLACK)

                    card.alpha = 0.45f
                    card.isEnabled = false
                    card.isClickable = false
                    card.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                }
            }
        }
    }
    // ================= PORTER STYLE CLICK =================
    private fun setupClickListeners() {

        setPorterClick(bikeEvCard, "TWO_WHEELER_EV")
        setPorterClick(bikePetrolCard, "TWO_WHEELER_PETROL")
        setPorterClick(loaderEvCard, "THREE_WHEELER_EV")
        setPorterClick(loaderPetrolCard, "THREE_WHEELER_PETROL")
        setPorterClick(loaderCngCard, "THREE_WHEELER_CNG")
        setPorterClick(truckEvCard, "FOUR_WHEELER_EV")
        setPorterClick(truckPetrolCard, "FOUR_WHEELER_PETROL")
        setPorterClick(truckCngCard, "FOUR_WHEELER_CNG")

        proceedBtn.setOnClickListener {
            if (selectedVehicle.isEmpty()) return@setOnClickListener
            vm.selectedVehicle = selectedVehicle
            vm.finalFare = fareMap[selectedVehicle] ?: 0.0
            startActivity(Intent(this, FinalFareActivity::class.java))
        }
    }

    private fun setPorterClick(card: CardView, type: String) {
        card.setOnClickListener {
            if (!fareMap.containsKey(type)) {
                return@setOnClickListener
            }
            if (selectedVehicle == type) {
                VehicleInfoDialog.show(
                    context = this,
                    vehicleType = type,
                    fare = fareMap[type] ?: 0.0
                )
            } else {
                selectVehicle(card, type)
            }
        }
    }

    // ================= SELECTION UI =================
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
        proceedBtn.text = "Proceed with ${formatVehicleText(type)}"
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

    private fun formatVehicleText(type: String): String {
        return when (type) {
            "TWO_WHEELER_EV" -> "2 Wheeler (EV)"
            "TWO_WHEELER_PETROL" -> "2 Wheeler (Petrol)"
            "THREE_WHEELER_EV" -> "3 Wheeler (EV)"
            "THREE_WHEELER_PETROL" -> "3 Wheeler (Petrol)"
            "THREE_WHEELER_CNG" -> "3 Wheeler (CNG)"
            "FOUR_WHEELER_EV" -> "4 Wheeler (EV)"
            "FOUR_WHEELER_PETROL" -> "4 Wheeler (Petrol)"
            "FOUR_WHEELER_CNG" -> "4 Wheeler (CNG)"
            else -> type
        }
    }

}
