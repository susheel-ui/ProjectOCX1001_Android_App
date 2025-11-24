package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import kotlinx.coroutines.*

class WaitingForApprovalActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var pickupInfo: TextView
    private lateinit var dropInfo: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var fareInfo: TextView
    private lateinit var progressBar: ProgressBar

    private var pickupPoint: GeoPoint? = null
    private var dropPoint: GeoPoint? = null
    private var selectedVehicle: String = "Bike"
    private var selectedFare: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.waiting_for_approval)

        // Initialize Views
        mapView = findViewById(R.id.mapView)
        pickupInfo = findViewById(R.id.pickupInfo)
        dropInfo = findViewById(R.id.dropInfo)
        vehicleInfo = findViewById(R.id.vehicleInfo)
        fareInfo = findViewById(R.id.fareInfo)       // 🟢 FIXED
        progressBar = findViewById(R.id.progressBar)

        // Get intent data
        val pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        val pickupLon = intent.getDoubleExtra("pickupLon", 0.0)
        val dropLat = intent.getDoubleExtra("dropLat", 0.0)
        val dropLon = intent.getDoubleExtra("dropLon", 0.0)
        selectedVehicle = intent.getStringExtra("vehicle") ?: "Bike"
        selectedFare = intent.getDoubleExtra("fare", 0.0)

        pickupPoint = GeoPoint(pickupLat, pickupLon)
        dropPoint = GeoPoint(dropLat, dropLon)

        // Set UI info
        pickupInfo.text = "Pickup: $pickupLat, $pickupLon"
        dropInfo.text = "Drop: $dropLat, $dropLon"
        vehicleInfo.text = "Vehicle: $selectedVehicle"
        fareInfo.text = "Fare: ₹${String.format("%.2f", selectedFare)}"

        setupMap()
        pollDriverApproval()
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        pickupPoint?.let {
            val marker = Marker(mapView)
            marker.position = it
            marker.title = "Pickup"
            mapView.overlays.add(marker)
        }

        dropPoint?.let {
            val marker = Marker(mapView)
            marker.position = it
            marker.title = "Drop"
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    private fun pollDriverApproval() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000)

                val driverAccepted = simulateDriverAcceptance()

                if (driverAccepted) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@WaitingForApprovalActivity, DriverDetailsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    break
                }
            }
        }
    }

    private fun simulateDriverAcceptance(): Boolean {
        return (1..20).random() == 1
    }
}
