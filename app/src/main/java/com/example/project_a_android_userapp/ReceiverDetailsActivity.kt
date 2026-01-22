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

    // 🔹 added buttons
    private var backButton: ImageButton? = null
    private var changeAddressButton: Button? = null

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

        // 🔹 bind views
        backButton = findViewById(R.id.BackButton)
        changeAddressButton = findViewById(R.id.changeAddressButton)

        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        centerPin = findViewById(R.id.centerPin)
        confirmButton = findViewById(R.id.confirmButton)

        houseEdit = findViewById(R.id.houseEdit)
        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)

        // ✅ AUTO-FILL FROM VM
        houseEdit?.setText(vm.receiverHouse)

        // 🔥 AUTO FILL SENDER DETAILS
        if (vm.senderName.isNotBlank()) {
            nameEdit?.setText(vm.senderName)
        }
        if (vm.senderPhone.isNotBlank()) {
            phoneEdit?.setText(vm.senderPhone)
        }

        // 🔥 AUTO SELECT RADIO BUTTON FROM VM
        if (vm.receiverType.isNotBlank()) {
            for (i in 0 until (typeRadioGroup?.childCount ?: 0)) {
                val rb = typeRadioGroup?.getChildAt(i)
                if (rb is RadioButton && rb.text.toString() == vm.receiverType) {
                    rb.isChecked = true
                    break
                }
            }
        }

        // 🔹 BACK BUTTON → SenderDetailsActivity
        backButton?.setOnClickListener {
            startActivity(
                Intent(this, SenderDetailsActivity::class.java)
            )
            finish()
        }

        // 🔹 CHANGE ADDRESS → Pickup/Drop selector
        changeAddressButton?.setOnClickListener {
            startActivity(
                Intent(this, Pickup_Drop_Selector_Activity::class.java)
            )
        }

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

    private fun shortAddressWords(address: String, maxWords: Int = 3): String {
        return address.split(" ").take(maxWords).joinToString(" ")
    }

    private fun fetchAddress(latLng: LatLng) {
        Thread {
            try {
                val geo = Geocoder(this, Locale.getDefault())
                val list = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val addr = list?.get(0)?.getAddressLine(0) ?: "Unknown Location"
                dropAddress = addr
                runOnUiThread {
                    addressShort?.text = shortAddressWords(dropAddress, 2)
                    addressFull?.text = shortAddressWords(dropAddress, 3)
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    private fun onConfirmClicked() {
        val missing = mutableListOf<Int>()
        if (nameEdit != null && nameEdit!!.text.toString().trim().isEmpty())
            missing.add(R.id.nameEdit)
        if (phoneEdit != null && phoneEdit!!.text.toString().trim().isEmpty())
            missing.add(R.id.phoneEdit)

        if (missing.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Missing details")
                .setMessage("Name or phone missing. Fill now or skip.")
                .setPositiveButton("Fill Now") { d, _ ->
                    d.dismiss()
                    when (missing[0]) {
                        R.id.nameEdit -> nameEdit?.requestFocus()
                        R.id.phoneEdit -> phoneEdit?.requestFocus()
                    }
                }
                .setNeutralButton("Skip") { d, _ ->
                    d.dismiss()
                    saveAndGoFare()
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .show()
            return
        }
        saveAndGoFare()
    }

    private fun saveAndGoFare() {
        vm.dropLat = dropLat
        vm.dropLon = dropLon
        vm.dropAddress = dropAddress

        houseEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            vm.receiverHouse = it
        }

        nameEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            vm.receiverName = it
        }

        phoneEdit?.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            vm.receiverPhone = it
        }

        typeRadioGroup?.let { rg ->
            val id = rg.checkedRadioButtonId
            if (id != -1) {
                findViewById<RadioButton>(id)?.text?.toString()?.let {
                    vm.receiverType = it
                }
            }
        }

        startActivity(Intent(this, FareActivity::class.java))
    }
}
