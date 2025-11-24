package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FinalFareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        val vehicle = intent.getStringExtra("vehicle") ?: "Bike"
        val baseFare = intent.getDoubleExtra("fare", 0.0)

        val pickupLat = intent.getDoubleExtra("pickupLat", 0.0)
        val pickupLon = intent.getDoubleExtra("pickupLon", 0.0)
        val dropLat = intent.getDoubleExtra("dropLat", 0.0)
        val dropLon = intent.getDoubleExtra("dropLon", 0.0)

        // GST Calculation
        val gst = baseFare * 0.18
        val finalFare = baseFare + gst

        // Views
        val vehicleImage = findViewById<ImageView>(R.id.vehicleImage)
        val fareText = findViewById<TextView>(R.id.finalFareText)

        val rule1 = findViewById<TextView>(R.id.rule1)
        val rule2 = findViewById<TextView>(R.id.rule2)
        val rule3 = findViewById<TextView>(R.id.rule3)
        val rule4 = findViewById<TextView>(R.id.rule4)
        val rule5 = findViewById<TextView>(R.id.rule5)
        val rule6 = findViewById<TextView>(R.id.rule6)
        val rule7 = findViewById<TextView>(R.id.rule7)

        val bookButton = findViewById<Button>(R.id.bookNowButton)

        // Set Fare
        fareText.text = "₹${String.format("%.2f", finalFare)}"

        // Set Rules
        rule1.text = "Fare doesn't include labour charges for loading & unloading."
        rule2.text = "Fare includes 25 mins free loading/unloading time."
        rule3.text = "Extra time will be chargeable."
        rule4.text = "Fare may change if route or location changes."
        rule5.text = "Parking charges to be paid by customer."
        rule6.text = "Fare includes toll and permit charges, if any."
        rule7.text = "We don't allow overloading."

        // Image by vehicle
        when (vehicle) {
            "Bike" -> vehicleImage.setImageResource(R.drawable.scooter)
            "Truck" -> vehicleImage.setImageResource(R.drawable.truck_10ft)
            "Big Truck" -> vehicleImage.setImageResource(R.drawable.truck_17ft)
        }

        // Button Action
        bookButton.setOnClickListener {
            val intent = Intent(this, WaitingForApprovalActivity::class.java)
            intent.putExtra("pickupLat", pickupLat)
            intent.putExtra("pickupLon", pickupLon)
            intent.putExtra("dropLat", dropLat)
            intent.putExtra("dropLon", dropLon)
            intent.putExtra("vehicle", vehicle)
            intent.putExtra("fare", finalFare)
            startActivity(intent)
            finish()
        }
    }
}
