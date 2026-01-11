package com.example.project_a_android_userapp

import android.annotation.SuppressLint
import android.content.Intent
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
    private lateinit var changeAddressButton: TextView

    private lateinit var houseEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var checkSavePhone: CheckBox
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var confirmButton: Button

    private lateinit var gMap: GoogleMap

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_details)

        vm = (application as MyApp).vm

        // -------- VIEWS --------
        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        changeAddressButton = findViewById(R.id.changeAddressButton)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        checkSavePhone = findViewById(R.id.checkSavePhone)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        confirmButton = findViewById(R.id.confirmButton)

        findViewById<ImageButton>(R.id.BackButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // -------- PICKUP ADDRESS --------
        addressShort.text =
            if (vm.pickupAddress.isNotEmpty())
                vm.pickupAddress.split(",").take(2).joinToString(", ")
            else "Pickup location"

        addressFull.text =
            if (vm.pickupAddress.isNotEmpty())
                vm.pickupAddress
            else "Pickup address"

        // -------- OPTIONAL PHONE (AUTO-FILL ONLY) --------
        val savedPhone = LocalStorage.getPhone(this)
        if (!savedPhone.isNullOrEmpty()) {
            phoneEdit.setText(savedPhone)
            checkSavePhone.isChecked = true   // show intent clearly
        }

        // -------- CHANGE ADDRESS --------
        changeAddressButton.setOnClickListener {
            startActivity(Intent(this, Pickup_Drop_Selector_Activity::class.java))
            finish()
        }

        // -------- MAP --------
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.miniMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // -------- CONFIRM --------
        confirmButton.setOnClickListener {

            val house = houseEdit.text.toString().trim()
            val name = nameEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val typeId = typeRadioGroup.checkedRadioButtonId

            if (name.isEmpty() || typeId == -1) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save into ViewModel
            vm.senderHouse = house
            vm.senderName = name
            vm.senderPhone = phone
            vm.senderType = findViewById<RadioButton>(typeId).text.toString()

            // ✅ Save phone ONLY if checkbox checked
            if (checkSavePhone.isChecked && phone.isNotEmpty()) {
                LocalStorage.savePhone(this, phone)
            }

            startActivity(Intent(this, ReceiverDetailsActivity::class.java))
        }
    }

    // -------- MAP READY --------
    override fun onMapReady(map: GoogleMap) {
        gMap = map

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
