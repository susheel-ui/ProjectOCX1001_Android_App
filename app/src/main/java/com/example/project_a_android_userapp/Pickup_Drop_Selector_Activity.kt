package com.example.project_a_android_userapp

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class Pickup_Drop_Selector_Activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private lateinit var gMap: GoogleMap
    private lateinit var pickupEdit: EditText
    private lateinit var dropEdit: EditText
    private lateinit var btnNext: Button
    private lateinit var pickupPin: ImageView
    private lateinit var dropPin: ImageView
    private val REQ_PICKUP = 1001
    private val REQ_DROP = 1002
    private var isSelectingPickup = false
    private var isSelectingDrop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_drop_selector)

        vm = (application as MyApp).vm

        pickupEdit = findViewById(R.id.pickupEdit)
        dropEdit = findViewById(R.id.dropEdit)
        btnNext = findViewById(R.id.btnNext)

        pickupPin = findViewById(R.id.pickupPin)
        dropPin = findViewById(R.id.dropPin)

        pickupPin.visibility = View.GONE
        dropPin.visibility = View.GONE

        // ⭐ Initialize Google Places
        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                "AIzaSyA6ck6zfcNbJZJnWBJCqbLWZgm98go0Dwg"
            )
        }

        // ⭐ Load Google Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ⭐ Click field → open search
        pickupEdit.setOnClickListener {
            isSelectingPickup = true
            isSelectingDrop = false
            pickupPin.visibility = View.VISIBLE
            dropPin.visibility = View.GONE
            openAutocomplete(true)
        }

        dropEdit.setOnClickListener {
            isSelectingPickup = false
            isSelectingDrop = true
            pickupPin.visibility = View.GONE
            dropPin.visibility = View.VISIBLE
            openAutocomplete(false)
        }

        // ⭐ Next button
        btnNext.setOnClickListener {
            if (vm.pickupAddress.isEmpty()) {
                Toast.makeText(this, "Select Pickup Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (vm.dropAddress.isEmpty()) {
                Toast.makeText(this, "Select Drop Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchDistanceAndTime {
                startActivity(Intent(this, SenderDetailsActivity::class.java))
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        val start = LatLng(25.44, 78.56)
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16f))

        // ⭐ When user stops dragging → get center location
        gMap.setOnCameraIdleListener {
            val center = gMap.cameraPosition.target
            updateLocationFromMap(center)
        }
    }

    // ⭐ Update pickup/drop on map drag (Marker stays fixed)
    private fun updateLocationFromMap(latLng: LatLng) {

        val geocoder = Geocoder(this, Locale.getDefault())
        val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        if (list != null && list.isNotEmpty()) {
            val address = list[0].getAddressLine(0)
            val shortAddress = makeShortAddress(address ?: "")

            if (isSelectingPickup) {
                vm.pickupLat = latLng.latitude
                vm.pickupLon = latLng.longitude
                vm.pickupAddress = shortAddress
                pickupEdit.setText(shortAddress)

            } else if (isSelectingDrop) {
                vm.dropLat = latLng.latitude
                vm.dropLon = latLng.longitude
                vm.dropAddress = shortAddress
                dropEdit.setText(shortAddress)
            }
        }
    }

    // ⭐ Autocomplete with Jhansi priority
    private fun openAutocomplete(isPickup: Boolean) {

        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val bounds = RectangularBounds.newInstance(
            LatLng(25.30, 78.40),
            LatLng(25.60, 78.75)
        )

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fields
        )
            .setLocationBias(bounds)
            .build(this)

        startActivityForResult(intent, if (isPickup) REQ_PICKUP else REQ_DROP)
    }

    // ⭐ Autocomplete Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            val latLng = place.latLng!!
            val finalAddress = makeShortAddress(place.address ?: "")

            if (requestCode == REQ_PICKUP) {

                isSelectingPickup = true
                isSelectingDrop = false
                pickupPin.visibility = View.VISIBLE
                dropPin.visibility = View.GONE

                vm.pickupLat = latLng.latitude
                vm.pickupLon = latLng.longitude
                vm.pickupAddress = finalAddress
                pickupEdit.setText(finalAddress)

            } else {

                isSelectingPickup = false
                isSelectingDrop = true
                pickupPin.visibility = View.GONE
                dropPin.visibility = View.VISIBLE

                vm.dropLat = latLng.latitude
                vm.dropLon = latLng.longitude
                vm.dropAddress = finalAddress
                dropEdit.setText(finalAddress)
            }

            // keep marker fixed, only move map
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }
    }

    // ⭐ Short Address Cleaner
    private fun makeShortAddress(full: String): String {
        val parts = full.split(",")
        return if (parts.size >= 2) "${parts[0].trim()}, ${parts[1].trim()}" else full
    }

    // ⭐ Google Directions API
    private fun fetchDistanceAndTime(onComplete: () -> Unit) {

        val apiKey = getString(R.string.directions_key)

        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${vm.pickupLat},${vm.pickupLon}" +
                    "&destination=${vm.dropLat},${vm.dropLon}" +
                    "&key=$apiKey"

        Thread {
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)

                if (json.getString("status") != "OK") {
                    runOnUiThread { onComplete() }
                    return@Thread
                }

                val legs = json.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)

                vm.distanceText = legs.getJSONObject("distance").getString("text")
                vm.durationText = legs.getJSONObject("duration").getString("text")
                vm.distanceValue = legs.getJSONObject("distance").getInt("value")
                vm.durationValue = legs.getJSONObject("duration").getInt("value")

                runOnUiThread { onComplete() }

            } catch (_: Exception) {
                runOnUiThread { onComplete() }
            }
        }.start()
    }
}
