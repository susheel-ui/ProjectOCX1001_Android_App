package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class WaitingForApprovalActivity : AppCompatActivity() {

    private lateinit var pickupInfo: TextView
    private lateinit var dropInfo: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedVehicle: String = ""
    private var selectedFare: Double = 0.0  // Not used but kept for future if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.waiting_for_approval)

        // Initialize Views
        pickupInfo = findViewById(R.id.pickupInfo)
        dropInfo = findViewById(R.id.dropInfo)
        vehicleInfo = findViewById(R.id.vehicleInfo)
        progressBar = findViewById(R.id.progressBar)

        // Get intent data
        val pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        val pickupLon = intent.getDoubleExtra("pickupLon", 0.0)
        val dropLat = intent.getDoubleExtra("dropLat", 0.0)
        val dropLon = intent.getDoubleExtra("dropLon", 0.0)
        selectedVehicle = intent.getStringExtra("vehicle") ?: "Bike"

        // Set data to UI
        pickupInfo.text = "Pickup: $pickupLat, $pickupLon"
        dropInfo.text = "Drop: $dropLat, $dropLon"
        vehicleInfo.text = "Vehicle: $selectedVehicle"

        // Start searching simulation
        pollDriverApproval()
    }

    private fun pollDriverApproval() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(4000)

                val driverAccepted = (1..15).random() == 1

                if (driverAccepted) {
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(
                                this@WaitingForApprovalActivity,
                                DriverDetailsActivity::class.java
                            )
                        )
                        finish()
                    }
                    break
                }
            }
        }
    }
}
