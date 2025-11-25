package com.example.project_a_android_userapp

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*

class Pickup_Drop_Selector_Activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var pickupEdit: EditText
    private lateinit var dropEdit: EditText
    private lateinit var submitButton: Button

    private var isSelectingPickup = true

    private val PICKUP_REQUEST = 101
    private val DROP_REQUEST = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_drop_selector)

        // Initialize Places API
        Places.initialize(this, "YOUR_API_KEY")

        pickupEdit = findViewById(R.id.pickupEdit)
        dropEdit = findViewById(R.id.dropEdit)
        submitButton = findViewById(R.id.submitButton)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        pickupEdit.setOnClickListener { openAutocomplete(PICKUP_REQUEST) }
        dropEdit.setOnClickListener { openAutocomplete(DROP_REQUEST) }

        submitButton.setOnClickListener {
            if (pickupEdit.text.isEmpty()) {
                Toast.makeText(this, "Select Pickup", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dropEdit.text.isEmpty()) {
                Toast.makeText(this, "Select Drop", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, SenderDetailsActivity::class.java)
            intent.putExtra("pickupAddress", pickupEdit.text.toString())
            intent.putExtra("dropAddress", dropEdit.text.toString())
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        val default = LatLng(25.4489, 78.5683)
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(default, 16f))

        gMap.setOnCameraIdleListener {
            val center = gMap.cameraPosition.target
            val address = getAddress(center)

            if (isSelectingPickup) {
                pickupEdit.setText(address)
            } else {
                dropEdit.setText(address)
            }
        }

        // Make user switch manually using click
        pickupEdit.setOnFocusChangeListener { _, focused ->
            if (focused) isSelectingPickup = true
        }

        dropEdit.setOnFocusChangeListener { _, focused ->
            if (focused) isSelectingPickup = false
        }
    }

    private fun getAddress(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            list?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun openAutocomplete(req: Int) {
        val fields = listOf(
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)

        startActivityForResult(intent, req)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)

            when (requestCode) {
                PICKUP_REQUEST -> {
                    pickupEdit.setText(place.address)
                    place.latLng?.let { gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f)) }
                }
                DROP_REQUEST -> {
                    dropEdit.setText(place.address)
                    place.latLng?.let { gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f)) }
                }
            }
        }
    }
}
