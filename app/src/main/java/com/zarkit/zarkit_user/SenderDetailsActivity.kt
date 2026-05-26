package com.zarkit.zarkit_user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class SenderDetailsActivity : BaseActivity(), OnMapReadyCallback {

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

        window.statusBarColor = android.graphics.Color.TRANSPARENT

        addressShort   = findViewById(R.id.addressShort)
        addressFull    = findViewById(R.id.addressLabel)
        houseEdit      = findViewById(R.id.houseEdit)
        nameEdit       = findViewById(R.id.nameEdit)
        phoneEdit      = findViewById(R.id.phoneEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        confirmButton  = findViewById(R.id.confirmButton)

        val tooltip    = findViewById<View>(R.id.infoTooltip)
        val helpButton = findViewById<TextView>(R.id.helpButton)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // CLOSE button
        findViewById<TextView>(R.id.closeTooltip).setOnClickListener {
            tooltip.animate()
                .alpha(0f)
                .translationY(-10f)
                .setDuration(200)
                .withEndAction {
                    tooltip.visibility = View.GONE
                    helpButton.visibility = View.VISIBLE
                    helpButton.alpha = 0f
                    helpButton.scaleX = 0.5f
                    helpButton.scaleY = 0.5f
                    helpButton.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(250)
                        .start()
                }
                .start()
        }

        // HELP ? button
        helpButton.setOnClickListener {
            helpButton.animate()
                .alpha(0f)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(180)
                .withEndAction {
                    helpButton.visibility = View.GONE
                    tooltip.visibility = View.VISIBLE
                    tooltip.alpha = 0f
                    tooltip.translationY = -10f
                    tooltip.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(220)
                        .start()
                }
                .start()
        }



        // Auto-fill phone from LocalStorage
        val savedPhone = LocalStorage.getPhone(this)
        if (!savedPhone.isNullOrEmpty()) {
            phoneEdit.setText(savedPhone)
        }

        // Default radio
        findViewById<RadioButton>(R.id.homeRadio).isChecked = true

        // Auto-fill pickup address
        val pickupAddress = LocalStorage.getPickupAddress(this)
        if (pickupAddress.isNotEmpty()) {
            addressShort.text = pickupAddress.split(",").firstOrNull() ?: "Pickup"
            addressFull.text  = pickupAddress
        } else {
            addressShort.text = "Pickup"
            addressFull.text  = "Pickup Address"
        }

        // Change Address
        findViewById<Button>(R.id.changeAddressButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Map init
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.miniMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Confirm
        confirmButton.setOnClickListener {
            val house  = houseEdit.text.toString().trim()
            val name   = nameEdit.text.toString().trim()
            val phone  = phoneEdit.text.toString().trim()
            val typeId = typeRadioGroup.checkedRadioButtonId

            if (house.isEmpty() || name.isEmpty() || phone.isEmpty() || typeId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            LocalStorage.saveSenderDetails(
                this,
                house = house,
                name  = name,
                phone = phone,
                type  = findViewById<RadioButton>(typeId).text.toString()
            )

            startActivity(Intent(this, ReceiverDetailsActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map

        val lat = LocalStorage.getPickupLat(this).takeIf { it != 0.0 } ?: 25.4489
        val lon = LocalStorage.getPickupLng(this).takeIf { it != 0.0 } ?: 78.5683
        val loc = LatLng(lat, lon)

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16f))

        val circle = gMap.addCircle(
            com.google.android.gms.maps.model.CircleOptions()
                .center(loc)
                .radius(0.0)
                .strokeColor(android.graphics.Color.parseColor("#FFD500"))
                .strokeWidth(3f)
                .fillColor(android.graphics.Color.parseColor("#33FFD500"))
        )

        val animator = android.animation.ValueAnimator.ofFloat(0f, 60f)
        animator.duration = 1000
        animator.repeatMode = android.animation.ValueAnimator.RESTART
        animator.repeatCount = android.animation.ValueAnimator.INFINITE
        animator.addUpdateListener { valueAnimator ->
            circle.radius = (valueAnimator.animatedValue as Float).toDouble()
        }
        animator.start()

        gMap.uiSettings.setAllGesturesEnabled(false)
    }
}