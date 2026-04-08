package com.zarkit.zarkit_user

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.util.*

class ReceiverDetailsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var vm: LocationViewModel
    private var gMap: GoogleMap? = null

    private lateinit var addressShort: TextView
    private lateinit var addressFull: TextView
    private lateinit var confirmButton: Button

    private lateinit var nameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var houseEdit: EditText
    private lateinit var typeRadioGroup: RadioGroup

    private var dropLat = 0.0
    private var dropLon = 0.0
    private var dropAddress = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // ✅ EDGE TO EDGE
        enableEdgeToEdge()
        setContentView(R.layout.activity_receiver_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        vm = (application as MyApp).vm

        // ===== Bind Views =====
        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        confirmButton = findViewById(R.id.confirmButton)

        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        houseEdit = findViewById(R.id.houseEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)

        findViewById<ImageButton>(R.id.BackButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.changeAddressButton).setOnClickListener {
            startActivity(Intent(this, Pickup_Drop_Selector_Activity::class.java))
        }

        // =========================================================
        //  AUTO COPY SENDER → RECEIVER (ONLY IF RECEIVER EMPTY)
        // =========================================================

        if (vm.receiverName.isBlank() && vm.senderName.isNotBlank()) {
            vm.receiverName = vm.senderName
        }

        if (vm.receiverPhone.isBlank() && vm.senderPhone.isNotBlank()) {
            vm.receiverPhone = vm.senderPhone
        }

        if (vm.receiverType.isBlank() && vm.senderType.isNotBlank()) {
            vm.receiverType = vm.senderType
        }

        // =========================================================
        //  SET UI FROM RECEIVER VALUES
        // =========================================================

        if (vm.receiverName.isNotBlank()) {
            nameEdit.setText(vm.receiverName)
        }

        if (vm.receiverPhone.isNotBlank()) {
            phoneEdit.setText(vm.receiverPhone)
        }

        if (vm.receiverHouse.isNotBlank()) {
            houseEdit.setText(vm.receiverHouse)
        }

        if (vm.receiverType.isNotBlank()) {
            for (i in 0 until typeRadioGroup.childCount) {
                val rb = typeRadioGroup.getChildAt(i)
                if (rb is RadioButton && rb.text.toString() == vm.receiverType) {
                    rb.isChecked = true
                    break
                }
            }
        }

        val frag = supportFragmentManager.findFragmentById(R.id.miniMapFragment)
        (frag as SupportMapFragment).getMapAsync(this)

        confirmButton.setOnClickListener {
            saveAndGoFare()
        }
    }

    // =========================================================
    // ================= MAP SECTION ===========================
    // =========================================================

    override fun onMapReady(map: GoogleMap) {

        gMap = map

        val lat = if (vm.dropLat != 0.0) vm.dropLat else 25.4489
        val lon = if (vm.dropLon != 0.0) vm.dropLon else 78.5683
        val location = LatLng(lat, lon)

        // Smooth camera animation
        gMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, 16f),
            1200,
            null
        )


        val circle = gMap?.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(location)
                .radius(0.0) // start radius
                .strokeColor(android.graphics.Color.parseColor("#FFD500")) // Google Blue
                .strokeWidth(3f)
                .fillColor(android.graphics.Color.parseColor("#33FFD500")) // semi-transparent Google Blue
        )

        val animator = ValueAnimator.ofFloat(0f, 80f)
        animator.duration = 1500
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener {
            circle?.radius = (it.animatedValue as Float).toDouble()
        }
        animator.start()

        gMap?.setOnCameraIdleListener {
            val target = gMap?.cameraPosition?.target
            target?.let {
                dropLat = it.latitude
                dropLon = it.longitude
                fetchAddress(it)
            }
        }

        gMap?.uiSettings?.setAllGesturesEnabled(true)
    }

    // =========================================================
    // ================= ADDRESS FETCH =========================
    // =========================================================

    private fun fetchAddress(latLng: LatLng) {
        Thread {
            try {
                val geo = Geocoder(this, Locale.getDefault())
                val list = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val addr = list?.get(0)?.getAddressLine(0) ?: "Unknown Location"
                dropAddress = addr

                runOnUiThread {
                    addressShort.text = addr.split(",").first()
                    addressFull.text = addr
                }

            } catch (_: Exception) {
            }
        }.start()
    }

    // =========================================================
    // ================= SAVE & NEXT ===========================
    // =========================================================

    private fun saveAndGoFare() {

        // Save map location
        vm.dropLat = dropLat
        vm.dropLon = dropLon
        vm.dropAddress = dropAddress

        // Save updated receiver values
        vm.receiverName = nameEdit.text.toString().trim()
        vm.receiverPhone = phoneEdit.text.toString().trim()
        vm.receiverHouse = houseEdit.text.toString().trim()

        val selectedId = typeRadioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val rb = findViewById<RadioButton>(selectedId)
            vm.receiverType = rb.text.toString()
        }

        startActivity(Intent(this, FareActivity::class.java))
    }
}
