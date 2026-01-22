package com.example.project_a_android_userapp

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
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

    private var isSelectingPickup = false
    private var isSelectingDrop = false

    // ================= ACTIVITY RESULT =================

    private val pickupLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleAutocompleteResult(it, true)
        }

    private val dropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleAutocompleteResult(it, false)
        }

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

        if (vm.pickupAddress.isNotEmpty()) {
            pickupEdit.setText(vm.pickupAddress)
            pickupPin.visibility = View.VISIBLE
            isSelectingPickup = true
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        btnNext.setOnClickListener {
            if (vm.pickupAddress.isEmpty()) {
                toast("Select Pickup Address")
                return@setOnClickListener
            }
            if (vm.dropAddress.isEmpty()) {
                toast("Select Drop Address")
                return@setOnClickListener
            }

            fetchDistanceAndTime {
                startActivity(Intent(this, SenderDetailsActivity::class.java))
            }
        }
    }

    // ================= MAP =================

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        if (vm.pickupLat != 0.0 && vm.pickupLon != 0.0) {
            val pickup = LatLng(vm.pickupLat, vm.pickupLon)
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 17f))
            pickupPin.visibility = View.VISIBLE
            isSelectingPickup = true
        } else {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.44, 78.56), 16f))
        }

        gMap.setOnCameraIdleListener {
            updateLocationFromMap(gMap.cameraPosition.target)
        }
    }

    // ================= MAP DRAG =================

    private fun updateLocationFromMap(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!list.isNullOrEmpty()) {
                val address = makeShortAddress(list[0].getAddressLine(0) ?: "")

                if (isSelectingPickup) {
                    vm.pickupLat = latLng.latitude
                    vm.pickupLon = latLng.longitude
                    vm.pickupAddress = address
                    pickupEdit.setText(address)
                }

                if (isSelectingDrop) {
                    vm.dropLat = latLng.latitude
                    vm.dropLon = latLng.longitude
                    vm.dropAddress = address
                    dropEdit.setText(address)
                }
            }
        } catch (_: Exception) {
        }
    }

    // ================= AUTOCOMPLETE =================

    private fun openAutocomplete(isPickup: Boolean) {

        val fields = listOf(
            Place.Field.ID,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        // ✅ Jhansi bounding box (approx)
        val jhansiBounds = RectangularBounds.newInstance(
            LatLng(25.40, 78.45), // SW
            LatLng(25.55, 78.65)  // NE
        )

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fields
        )
            // 🔥 BIAS RESULTS TO JHANSI
            .setLocationBias(jhansiBounds)

            // 🔥 INDIA ONLY
            .setCountries(listOf("IN"))

            .build(this)

        if (isPickup) {
            pickupLauncher.launch(intent)
        } else {
            dropLauncher.launch(intent)
        }
    }


    private fun handleAutocompleteResult(
        result: ActivityResult,
        isPickup: Boolean
    ) {
        if (result.resultCode != RESULT_OK || result.data == null) return

        val place = Autocomplete.getPlaceFromIntent(result.data!!)
        val latLng = place.latLng ?: return
        val address = makeShortAddress(place.address ?: "")

        if (isPickup) {
            vm.pickupLat = latLng.latitude
            vm.pickupLon = latLng.longitude
            vm.pickupAddress = address
            pickupEdit.setText(address)
            pickupPin.visibility = View.VISIBLE
            dropPin.visibility = View.GONE
        } else {
            vm.dropLat = latLng.latitude
            vm.dropLon = latLng.longitude
            vm.dropAddress = address
            dropEdit.setText(address)
            pickupPin.visibility = View.GONE
            dropPin.visibility = View.VISIBLE
        }

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    // ================= DISTANCE =================

    private fun fetchDistanceAndTime(onComplete: () -> Unit) {

        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${vm.pickupLat},${vm.pickupLon}" +
                    "&destination=${vm.dropLat},${vm.dropLon}" +
                    "&key=${getString(R.string.google_maps_key)}"

        Thread {
            try {
                val json = JSONObject(URL(url).readText())
                val legs = json.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)

                vm.distanceText = legs.getJSONObject("distance").getString("text")
                vm.durationText = legs.getJSONObject("duration").getString("text")
                vm.distanceValue = legs.getJSONObject("distance").getInt("value")
                vm.durationValue = legs.getJSONObject("duration").getInt("value")

            } catch (_: Exception) {
            }

            runOnUiThread { onComplete() }
        }.start()
    }

    private fun makeShortAddress(full: String): String {
        val parts = full.split(",")
        return if (parts.size >= 2)
            "${parts[0].trim()}, ${parts[1].trim()}"
        else full
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
