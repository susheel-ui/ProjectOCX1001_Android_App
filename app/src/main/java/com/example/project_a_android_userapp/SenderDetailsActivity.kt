package com.example.project_a_android_userapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.project_a_android_userapp.LocalStorage


class SenderDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private lateinit var addressShort: TextView
    private lateinit var addressFull: TextView

    private lateinit var houseEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var confirmButton: Button

    private lateinit var gMap: GoogleMap

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_details)

        vm = (application as MyApp).vm

        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        confirmButton = findViewById(R.id.confirmButton)

        // Back button
        findViewById<ImageButton>(R.id.BackButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Auto-fill phone from LocalStorage
        val savedPhone = LocalStorage.getPhone(this)
        if (!savedPhone.isNullOrEmpty()) {
            phoneEdit.setText(savedPhone)
        }

        // Default radio button = Home
        findViewById<RadioButton>(R.id.homeRadio).isChecked = true

        // Auto-fill address from VM
        if (vm.pickupAddress.isNotEmpty()) {
            addressShort.text = vm.pickupAddress.split(",").firstOrNull() ?: "Pickup"
            addressFull.text = vm.pickupAddress
        } else {
            addressShort.text = "Pickup"
            addressFull.text = "Pickup Address"
        }

        // Change Address button → go back
        findViewById<Button>(R.id.changeAddressButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize map
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.miniMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Confirm button click
        confirmButton.setOnClickListener {
            val house = houseEdit.text.toString().trim()
            val name = nameEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val typeId = typeRadioGroup.checkedRadioButtonId

            if (house.isEmpty() || name.isEmpty() || phone.isEmpty() || typeId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save sender details into VM
            vm.senderHouse = house
            vm.senderName = name
            vm.senderPhone = phone
            vm.senderType = findViewById<RadioButton>(typeId).text.toString()

            // Go to receiver screen
            startActivity(android.content.Intent(this, ReceiverDetailsActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        // If VM has pickup coordinates, show marker there; otherwise default
        val lat = if (vm.pickupLat != 0.0) vm.pickupLat else 25.4489
        val lon = if (vm.pickupLon != 0.0) vm.pickupLon else 78.5683
        val loc = LatLng(lat, lon)
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16f))

        gMap.addMarker(
            MarkerOptions()
                .position(loc)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        gMap.uiSettings.setAllGesturesEnabled(false)
    }
}
