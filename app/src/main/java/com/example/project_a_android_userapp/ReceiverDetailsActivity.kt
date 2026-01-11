package com.example.project_a_android_userapp

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

class ReceiverDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private var gMap: GoogleMap? = null

    private lateinit var addressShort: TextView
    private lateinit var addressFull: TextView
    private lateinit var centerPin: ImageView
    private lateinit var confirmButton: Button

    private lateinit var houseEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var checkSavePhone: CheckBox
    private lateinit var typeRadioGroup: RadioGroup

    private var dropLat = 0.0
    private var dropLon = 0.0
    private var dropAddress = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_details)

        vm = (application as MyApp).vm

        // -------- VIEWS --------
        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        centerPin = findViewById(R.id.centerPin)
        confirmButton = findViewById(R.id.confirmButton)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        checkSavePhone = findViewById(R.id.checkSavePhone)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)

        findViewById<ImageButton>(R.id.BackButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // -------- PREFILL FROM VM --------
        houseEdit.setText(vm.receiverHouse)
        nameEdit.setText(vm.receiverName)
        phoneEdit.setText(vm.receiverPhone)

        // -------- AUTO-FILL PHONE FROM LOCAL STORAGE --------
        val savedPhone = LocalStorage.getPhone(this)
        if (!savedPhone.isNullOrEmpty() && phoneEdit.text.isNullOrEmpty()) {
            phoneEdit.setText(savedPhone)
            checkSavePhone.isChecked = true
        }

        // -------- MAP --------
        val frag = supportFragmentManager.findFragmentById(R.id.miniMapFragment)
        if (frag !is SupportMapFragment) {
            Toast.makeText(this, "Map fragment missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        frag.getMapAsync(this)

        confirmButton.setOnClickListener { onConfirmClicked() }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        val lat = if (vm.dropLat != 0.0) vm.dropLat else 25.4489
        val lon = if (vm.dropLon != 0.0) vm.dropLon else 78.5683
        val start = LatLng(lat, lon)

        gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16f))

        gMap?.setOnCameraIdleListener {
            animatePin()
            val target = gMap?.cameraPosition?.target
            if (target != null) {
                dropLat = target.latitude
                dropLon = target.longitude
                fetchAddress(target)
            }
        }
    }

    private fun animatePin() {
        val anim = TranslateAnimation(0f, 0f, -35f, 0f)
        anim.duration = 250
        centerPin.startAnimation(anim)
    }

    private fun fetchAddress(latLng: LatLng) {
        Thread {
            try {
                val geo = Geocoder(this, Locale.getDefault())
                val list = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val addr = list?.get(0)?.getAddressLine(0) ?: "Unknown location"
                dropAddress = addr

                runOnUiThread {
                    addressShort.text = addr.split(",").take(2).joinToString(", ")
                    addressFull.text = addr
                }
            } catch (_: Exception) {}
        }.start()
    }

    private fun onConfirmClicked() {
        val name = nameEdit.text.toString().trim()
        val phone = phoneEdit.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Receiver name required", Toast.LENGTH_SHORT).show()
            return
        }

        saveAndGoFare()
    }

    private fun saveAndGoFare() {

        // -------- SAVE DROP --------
        vm.dropLat = dropLat
        vm.dropLon = dropLon
        vm.dropAddress = dropAddress

        // -------- SAVE RECEIVER DETAILS --------
        vm.receiverHouse = houseEdit.text.toString().trim()
        vm.receiverName = nameEdit.text.toString().trim()
        vm.receiverPhone = phoneEdit.text.toString().trim()

        val typeId = typeRadioGroup.checkedRadioButtonId
        if (typeId != -1) {
            vm.receiverType = findViewById<RadioButton>(typeId).text.toString()
        }

        // -------- SAVE PHONE ONLY IF CHECKED --------
        if (checkSavePhone.isChecked && phoneEdit.text.isNotEmpty()) {
            LocalStorage.savePhone(this, phoneEdit.text.toString().trim())
        }

        startActivity(Intent(this, FareActivity::class.java))
    }
}
