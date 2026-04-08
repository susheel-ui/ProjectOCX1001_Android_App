package com.zarkit.zarkit_user

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import kotlin.math.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Pickup_Drop_Selector_Activity : BaseActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private lateinit var gMap: GoogleMap

    private lateinit var pickupEdit: EditText
    private lateinit var dropEdit: EditText
    private lateinit var btnNext: Button
    private lateinit var pickupPin: ImageView
    private lateinit var dropPin: ImageView

    private lateinit var btnBack: ImageView

    private var isSelectingPickup = false
    private var isSelectingDrop = false

    // 📍 JHANSI ELITE CENTER (REFERENCE POINT ONLY)
    private val JHANSI_LAT = 25.4484
    private val JHANSI_LNG = 78.5685
    private val MAX_RADIUS_KM = 40.0

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
        enableEdgeToEdge()
        setContentView(R.layout.activity_pickup_drop_selector)

        val root = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        vm = (application as MyApp).vm

        pickupEdit = findViewById(R.id.pickupEdit)
        dropEdit = findViewById(R.id.dropEdit)
        btnNext = findViewById(R.id.btnNext)
        pickupPin = findViewById(R.id.pickupPin)
        dropPin = findViewById(R.id.dropPin)

        pickupPin.visibility = View.GONE
        dropPin.visibility = View.GONE

        // KEEP EXISTING AUTO PICKUP FROM CURRENT LOCATION
        if (vm.pickupAddress.isNotEmpty()) {
            pickupEdit.setText(vm.pickupAddress)
            pickupPin.visibility = View.VISIBLE
            isSelectingPickup = true
        }
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            goToHome()
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
            validateAndProceed()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, Home_Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }


    // ================= MAP =================

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        // ✅ KEEP EXISTING CAMERA BEHAVIOR
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

        //  BLOCK OUTSIDE 40 KM FROM JHANSI ELITE
        if (!isWithinJhansi(latLng.latitude, latLng.longitude)) {
            showOutOfRangeDialog()
            return
        }

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

        val jhansiBounds = RectangularBounds.newInstance(
            LatLng(25.40, 78.45),
            LatLng(25.55, 78.65)
        )

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            fields
        )
            .setLocationBias(jhansiBounds)
            .setCountries(listOf("IN"))
            .build(this)

        intent.putExtra(
            "theme",
            R.style.PlacesWhiteTheme
        )

        if (isPickup) pickupLauncher.launch(intent)
        else dropLauncher.launch(intent)
    }

    private fun handleAutocompleteResult(
        result: ActivityResult,
        isPickup: Boolean
    ) {
        if (result.resultCode != RESULT_OK || result.data == null) return

        val place = Autocomplete.getPlaceFromIntent(result.data!!)
        val latLng = place.latLng ?: return

        //  BLOCK OUTSIDE 40 KM
        if (!isWithinJhansi(latLng.latitude, latLng.longitude)) {
            showOutOfRangeDialog()
            return
        }

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

    // ================= FINAL VALIDATION =================

    private fun validateAndProceed() {

        if (vm.pickupAddress.isEmpty()) {
            toast("Select Pickup Address")
            return
        }

        if (vm.dropAddress.isEmpty()) {
            toast("Select Drop Address")
            return
        }

        if (!isWithinJhansi(vm.pickupLat, vm.pickupLon)
            || !isWithinJhansi(vm.dropLat, vm.dropLon)
        ) {
            showOutOfRangeDialog()
            return
        }

        fetchDistanceAndTime {
            startActivity(Intent(this, SenderDetailsActivity::class.java))
        }
    }

    // ================= DISTANCE =================

    private fun isWithinJhansi(lat: Double, lng: Double): Boolean {
        return distanceFromJhansi(lat, lng) <= MAX_RADIUS_KM
    }

    private fun distanceFromJhansi(lat: Double, lng: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat - JHANSI_LAT)
        val dLng = Math.toRadians(lng - JHANSI_LNG)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(JHANSI_LAT)) *
                cos(Math.toRadians(lat)) *
                sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    // ================= API =================

    private fun fetchDistanceAndTime(onComplete: () -> Unit) {

        Log.d("DISTANCE_DEBUG", "FUNCTION CALLED")

        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${vm.pickupLat},${vm.pickupLon}" +
                    "&destination=${vm.dropLat},${vm.dropLon}" +
                    "&mode=driving" +
                    "&key=${("AIzaSyAk5HjRT_tihvIZ7Y0ZQbcvpzn0yOSM8ac")}"

        Thread {
            try {

                val response = URL(url).readText()
                val json = JSONObject(response)

                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) {
                    Log.e("DISTANCE_DEBUG", "No routes found")
                    runOnUiThread { onComplete() }
                    return@Thread
                }

                val legs = routes
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)

                val distanceObj = legs.getJSONObject("distance")
                val durationObj = legs.getJSONObject("duration")

                //  TEXT
                vm.distanceText = distanceObj.getString("text")
                vm.durationText = durationObj.getString("text")

                //  VALUE (Use Double)
                vm.distanceValue = distanceObj.getDouble("value")   // meters
                vm.durationValue = durationObj.getDouble("value")   // seconds

            } catch (e: Exception) {
                Log.e("DISTANCE_DEBUG", "ERROR = ${e.message}")
            }

            runOnUiThread { onComplete() }

        }.start()
    }


    // ================= UI =================

    private fun showOutOfRangeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Out of Service Area")
            .setMessage("Please select pickup and drop locations within Jhansi (40 KM).")
            .setPositiveButton("OK", null)
            .show()
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
