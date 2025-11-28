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
    private var addressShort: TextView? = null
    private var addressFull: TextView? = null
    private var centerPin: ImageView? = null
    private var confirmButton: Button? = null

    // optional fields
    private var houseEdit: EditText? = null
    private var nameEdit: EditText? = null
    private var phoneEdit: EditText? = null
    private var typeRadioGroup: RadioGroup? = null

    private var dropLat = 0.0
    private var dropLon = 0.0
    private var dropAddress = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_details)

        vm = (application as MyApp).vm

        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        centerPin = findViewById(R.id.centerPin)
        confirmButton = findViewById(R.id.confirmButton)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)

        // pre-fill optional fields from vm if available
        houseEdit?.setText(vm.receiverHouse)
        nameEdit?.setText(vm.receiverName)
        phoneEdit?.setText(vm.receiverPhone)
        // note: radioGroup pre-check omitted for brevity

        val frag = supportFragmentManager.findFragmentById(R.id.miniMapFragment)
        if (frag !is SupportMapFragment) {
            Toast.makeText(this, "Map fragment missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        frag.getMapAsync(this)

        confirmButton?.setOnClickListener { onConfirmClicked() }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        // start at vm's drop if present, otherwise default
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
        centerPin?.let {
            val anim = TranslateAnimation(0f, 0f, -35f, 0f)
            anim.duration = 250
            it.startAnimation(anim)
        }
    }

    private fun fetchAddress(latLng: LatLng) {
        Thread {
            try {
                val geo = Geocoder(this, Locale.getDefault())
                val list = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val addr = list?.get(0)?.getAddressLine(0) ?: "Unknown Location"
                dropAddress = addr
                runOnUiThread {
                    addressShort?.text = dropAddress
                    addressFull?.text = dropAddress
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    private fun onConfirmClicked() {
        // if optional fields are present & empty, ask user to fill or skip
        val missing = mutableListOf<Int>()
        if (nameEdit != null && nameEdit!!.text.toString().trim().isEmpty()) missing.add(R.id.nameEdit)
        if (phoneEdit != null && phoneEdit!!.text.toString().trim().isEmpty()) missing.add(R.id.phoneEdit)

        if (missing.isNotEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Missing details")
            builder.setMessage("Name or phone missing. Fill now or skip.")
            builder.setPositiveButton("Fill Now") { dlg, _ ->
                dlg.dismiss()
                when (missing[0]) {
                    R.id.nameEdit -> nameEdit?.requestFocus()
                    R.id.phoneEdit -> phoneEdit?.requestFocus()
                }
            }
            builder.setNeutralButton("Skip") { dlg, _ ->
                dlg.dismiss()
                saveAndGoFare()
            }
            builder.setNegativeButton("Cancel") { dlg, _ -> dlg.dismiss() }
            builder.show()
            return
        }
        saveAndGoFare()
    }

    private fun saveAndGoFare() {
        // Save drop to vm
        vm.dropLat = dropLat
        vm.dropLon = dropLon
        vm.dropAddress = dropAddress

        // Save optional receiver fields if present
        houseEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let { vm.receiverHouse = it }
        nameEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let { vm.receiverName = it }
        phoneEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let { vm.receiverPhone = it }
        typeRadioGroup?.let { rg ->
            val id = rg.checkedRadioButtonId
            if (id != -1) {
                findViewById<RadioButton>(id)?.text?.toString()?.let { vm.receiverType = it }
            }
        }

        // start FareActivity (it will read vm)
        startActivity(Intent(this, FareActivity::class.java))
    }
}
