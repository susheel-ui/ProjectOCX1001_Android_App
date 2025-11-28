package com.example.project_a_android_userapp

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class Pickup_Drop_Selector_Activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private lateinit var gMap: GoogleMap
    private lateinit var pickupEdit: EditText
    private lateinit var dropEdit: EditText
    private lateinit var pickupPin: ImageView
    private lateinit var dropPin: ImageView
    private lateinit var btnSelectPickup: Button
    private lateinit var btnSelectDrop: Button
    private lateinit var btnNext: Button

    private var mode = "" // "pickup" / "drop"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_drop_selector)

        // get shared vm from application
        vm = (application as MyApp).vm

        pickupEdit = findViewById(R.id.pickupEdit)
        dropEdit = findViewById(R.id.dropEdit)
        pickupPin = findViewById(R.id.pickupPin)
        dropPin = findViewById(R.id.dropPin)
        btnSelectPickup = findViewById(R.id.btnPickupSelect)
        btnSelectDrop = findViewById(R.id.btnDropSelect)
        btnNext = findViewById(R.id.btnNext)

        pickupPin.visibility = View.GONE
        dropPin.visibility = View.GONE

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSelectPickup.setOnClickListener {
            mode = "pickup"
            pickupPin.visibility = View.VISIBLE
            dropPin.visibility = View.GONE
            Toast.makeText(this, "Move map to set Pickup", Toast.LENGTH_SHORT).show()
        }

        btnSelectDrop.setOnClickListener {
            mode = "drop"
            dropPin.visibility = View.VISIBLE
            pickupPin.visibility = View.GONE
            Toast.makeText(this, "Move map to set Drop", Toast.LENGTH_SHORT).show()
        }

        btnNext.setOnClickListener {
            if (vm.pickupAddress.isEmpty()) {
                Toast.makeText(this, "Pickup not selected!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (vm.dropAddress.isEmpty()) {
                Toast.makeText(this, "Drop not selected!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // navigate to SenderDetailsActivity — no extras required
            startActivity(android.content.Intent(this, SenderDetailsActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map
        val start = LatLng(25.44, 78.56)
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16f))

        gMap.setOnCameraIdleListener {
            if (mode.isEmpty()) return@setOnCameraIdleListener
            val pos = gMap.cameraPosition.target
            animatePin()
            fetchAddress(pos)
        }
    }

    private fun animatePin() {
        val pin = if (mode == "pickup") pickupPin else dropPin
        val anim = TranslateAnimation(0f, 0f, -40f, 0f)
        anim.duration = 250
        pin.startAnimation(anim)
    }

    private fun fetchAddress(latLng: LatLng) {
        Thread {
            try {
                val geo = Geocoder(this, Locale.getDefault())
                val res = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val address = res?.get(0)?.getAddressLine(0) ?: ""

                runOnUiThread {
                    if (mode == "pickup") {
                        vm.pickupLat = latLng.latitude
                        vm.pickupLon = latLng.longitude
                        vm.pickupAddress = address
                        pickupEdit.setText(address)
                    } else {
                        vm.dropLat = latLng.latitude
                        vm.dropLon = latLng.longitude
                        vm.dropAddress = address
                        dropEdit.setText(address)
                    }
                }
            } catch (_: Exception) {
            }
        }.start()
    }
}
