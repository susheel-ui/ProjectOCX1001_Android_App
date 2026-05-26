package com.zarkit.zarkit_user

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
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
        setContentView(R.layout.activity_receiver_details)

        // ===== Bind Views =====
        addressShort = findViewById(R.id.addressShort)
        addressFull = findViewById(R.id.addressLabel)
        confirmButton = findViewById(R.id.confirmButton)

        nameEdit = findViewById(R.id.nameEdit)
        phoneEdit = findViewById(R.id.phoneEdit)
        houseEdit = findViewById(R.id.houseEdit)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)

        val tooltip = findViewById<View>(R.id.infoTooltip)
        val helpButton = findViewById<TextView>(R.id.helpButton)

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

        findViewById<ImageView>(R.id.BackButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.changeAddressButton).setOnClickListener {
            startActivity(Intent(this, Pickup_Drop_Selector_Activity::class.java))
        }

        // =========================================================
        //  AUTO COPY SENDER → RECEIVER (ONLY IF RECEIVER EMPTY)
        // =========================================================

        val savedReceiverName = LocalStorage.getReceiverName(this)
        val savedSenderName = LocalStorage.getSenderName(this)

        val savedReceiverPhone = LocalStorage.getReceiverPhone(this)
        val savedSenderPhone = LocalStorage.getSenderPhone(this)

        val savedReceiverType = LocalStorage.getReceiverType(this)
        val savedSenderType = LocalStorage.getSenderType(this)

        if (savedReceiverName.isBlank() && savedSenderName.isNotBlank()) {
            LocalStorage.saveReceiverDetails(
                this,
                house = LocalStorage.getReceiverHouse(this),
                name = savedSenderName,
                phone = LocalStorage.getReceiverPhone(this),
                type = LocalStorage.getReceiverType(this)
            )
        }

        if (savedReceiverPhone.isBlank() && savedSenderPhone.isNotBlank()) {
            LocalStorage.saveReceiverDetails(
                this,
                house = LocalStorage.getReceiverHouse(this),
                name = LocalStorage.getReceiverName(this),
                phone = savedSenderPhone,
                type = LocalStorage.getReceiverType(this)
            )
        }

        if (savedReceiverType.isBlank() && savedSenderType.isNotBlank()) {
            LocalStorage.saveReceiverDetails(
                this,
                house = LocalStorage.getReceiverHouse(this),
                name = LocalStorage.getReceiverName(this),
                phone = LocalStorage.getReceiverPhone(this),
                type = savedSenderType
            )
        }

        // =========================================================
        //  SET UI FROM RECEIVER VALUES
        // =========================================================

        val receiverName = LocalStorage.getReceiverName(this)
        val receiverPhone = LocalStorage.getReceiverPhone(this)
        val receiverHouse = LocalStorage.getReceiverHouse(this)
        val receiverType = LocalStorage.getReceiverType(this)

        if (receiverName.isNotBlank()) nameEdit.setText(receiverName)
        if (receiverPhone.isNotBlank()) phoneEdit.setText(receiverPhone)
        if (receiverHouse.isNotBlank()) houseEdit.setText(receiverHouse)

        if (receiverType.isNotBlank()) {
            for (i in 0 until typeRadioGroup.childCount) {
                val rb = typeRadioGroup.getChildAt(i)
                if (rb is RadioButton && rb.text.toString() == receiverType) {
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

        val lat = LocalStorage.getDropLat(this).takeIf { it != 0.0 } ?: 25.4489
        val lon = LocalStorage.getDropLng(this).takeIf { it != 0.0 } ?: 78.5683
        val location = LatLng(lat, lon)

        gMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, 16f),
            1200,
            null
        )

        val circle = gMap?.addCircle(
            CircleOptions()
                .center(location)
                .radius(0.0)
                .strokeColor(android.graphics.Color.parseColor("#FFD500"))
                .strokeWidth(3f)
                .fillColor(android.graphics.Color.parseColor("#33FFD500"))
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

        // Save updated drop location from map
        LocalStorage.saveDropLocation(this, dropLat, dropLon, dropAddress)

        // Save receiver details
        val selectedId = typeRadioGroup.checkedRadioButtonId
        val receiverType = if (selectedId != -1) {
            findViewById<RadioButton>(selectedId).text.toString()
        } else {
            ""
        }

        LocalStorage.saveReceiverDetails(
            this,
            house = houseEdit.text.toString().trim(),
            name = nameEdit.text.toString().trim(),
            phone = phoneEdit.text.toString().trim(),
            type = receiverType
        )

        startActivity(Intent(this, FareActivity::class.java))
    }
}