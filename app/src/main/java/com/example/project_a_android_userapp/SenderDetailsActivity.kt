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

        findViewById<ImageButton>(R.id.BackButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // show pickup from vm (may be empty)
        addressShort.text = if (vm.pickupAddress.isNotEmpty()) vm.pickupAddress.split(" ").take(2).joinToString(" ") else "Pickup"
        addressFull.text = if (vm.pickupAddress.isNotEmpty()) vm.pickupAddress else "Pickup Address"

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.miniMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        confirmButton.setOnClickListener {
            val house = houseEdit.text.toString().trim()
            val name = nameEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val typeId = typeRadioGroup.checkedRadioButtonId

            if (house.isEmpty() || name.isEmpty() || phone.isEmpty() || typeId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // save sender details into vm
            vm.senderHouse = house
            vm.senderName = name
            vm.senderPhone = phone
            vm.senderType = findViewById<RadioButton>(typeId).text.toString()

            // go to receiver screen (no extras)
            startActivity(android.content.Intent(this, ReceiverDetailsActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        // If vm has pickup coordinates, show marker there; otherwise default
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
