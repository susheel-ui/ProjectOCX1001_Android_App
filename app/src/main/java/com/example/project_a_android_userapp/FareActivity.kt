package com.example.project_a_android_userapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.ceil
import java.net.URLEncoder

class FareActivity : AppCompatActivity() {

    private lateinit var pickupText: TextView
    private lateinit var dropText: TextView
    private lateinit var distanceText: TextView

    private lateinit var bikeCard: CardView
    private lateinit var loaderCard: CardView
    private lateinit var truckCard: CardView

    private lateinit var bikeFareText: TextView
    private lateinit var loaderFareText: TextView
    private lateinit var truckFareText: TextView

    private lateinit var proceedBtn: Button

    private val vm by lazy { (application as MyApp).vm }

    private var selectedVehicle = "Bike"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fare)

        // Bind views
        pickupText = findViewById(R.id.pickupText)
        dropText = findViewById(R.id.dropText)
        distanceText = findViewById(R.id.distanceText)

        bikeCard = findViewById(R.id.vehicleBike)
        loaderCard = findViewById(R.id.vehicleLoader)
        truckCard = findViewById(R.id.vehicleTruck)

        bikeFareText = findViewById(R.id.bikeFareText)
        loaderFareText = findViewById(R.id.loaderFareText)
        truckFareText = findViewById(R.id.truckFareText)

        proceedBtn = findViewById(R.id.proceedBtn)

        // Populate UI
        pickupText.text = vm.pickupAddress
        dropText.text = vm.dropAddress

        // Click listeners
        bikeCard.setOnClickListener { selectVehicle(bikeCard, "Bike") }
        loaderCard.setOnClickListener { selectVehicle(loaderCard, "Loader") }
        truckCard.setOnClickListener { selectVehicle(truckCard, "Truck") }

        // Default
        selectVehicle(bikeCard, "Bike")

        // Distance API
        getDistanceFromAPI()
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

    private fun getDistanceFromAPI() {

        if (vm.pickupLat == 0.0 || vm.dropLat == 0.0) {
            Log.e("DISTANCE", "Invalid LAT LNG")
            return
        }

        val apiKey = "AIzaSyCG4YVvKPVB_nruoVtL8RqS0ek8kxp69Xw"

        // ************* FIXED URL *************
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${vm.pickupLat},${vm.pickupLon}" +
                    "&destination=${vm.dropLat},${vm.dropLon}" +
                    "&mode=driving" +
                    "&key=${URLEncoder.encode(apiKey, "UTF-8")}"

        Log.e("URL_CHECK", url)

        val client = OkHttpClient()

        Thread {
            try {
                val response = client.newCall(Request.Builder().url(url).build()).execute()
                val jsonResponse = response.body?.string() ?: ""

                val json = JSONObject(jsonResponse)

                val routes = json.optJSONArray("routes")
                if (routes == null || routes.length() == 0) {
                    Log.e("DISTANCE_ERROR", "NO ROUTES: $jsonResponse")
                    runOnUiThread { distanceText.text = "Distance: 0 km" }
                    return@Thread
                }

                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val dist = legs.getJSONObject(0).getJSONObject("distance")
                val distanceStr = dist.getString("text")     // e.g. "14.8 km"

                val km = distanceStr.replace(" km", "").toDouble()

                runOnUiThread { updateFare(km) }

            } catch (e: Exception) {
                Log.e("DISTANCE_ERROR", e.toString())
            }
        }.start()
    }

    private fun updateFare(km: Double) {
        val cleanKm = ceil(km)

        distanceText.text = "Distance: $cleanKm km"

        bikeFareText.text = "₹" + ceil(cleanKm * 12)
        loaderFareText.text = "₹" + ceil(cleanKm * 20)
        truckFareText.text = "₹" + ceil(cleanKm * 40)
    }
}
