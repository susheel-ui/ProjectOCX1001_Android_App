package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FinalFareActivity : AppCompatActivity() {

    private lateinit var vm: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        vm = (application as MyApp).vm

        // Vehicle from VM ONLY
        val vehicle = vm.selectedVehicle

        // Original Fare (without GST)
        val originalFare = vm.finalFare

        // Calculate GST here only
        val gst = originalFare * 0.18
        val finalFareWithGST = originalFare + gst

        // Views
        val vehicleImage = findViewById<ImageView>(R.id.vehicleImage)
        val gstFareText = findViewById<TextView>(R.id.finalFareText)
        val originalFareText = findViewById<TextView>(R.id.paymentFare)

        val rule1 = findViewById<TextView>(R.id.rule1)
        val rule2 = findViewById<TextView>(R.id.rule2)
        val rule3 = findViewById<TextView>(R.id.rule3)
        val rule4 = findViewById<TextView>(R.id.rule4)
        val rule5 = findViewById<TextView>(R.id.rule5)
        val rule6 = findViewById<TextView>(R.id.rule6)
        val rule7 = findViewById<TextView>(R.id.rule7)

        val bookButton = findViewById<Button>(R.id.bookNowButton)

        // Show original + GST prices
        originalFareText.text = "₹${String.format("%.2f", finalFareWithGST)}"
        gstFareText.text = "₹${String.format("%.2f", originalFare)}"

        // Vehicle Image
        when (vehicle) {
            "Bike" -> vehicleImage.setImageResource(R.drawable.mini_3w)
            "Loader" -> vehicleImage.setImageResource(R.drawable.loader)
            "Truck" -> vehicleImage.setImageResource(R.drawable.truck)
        }

        // Rules
        rule1.text = "Fare doesn't include labour charges for loading & unloading."
        rule2.text = "Fare includes 25 mins free loading/unloading time."
        rule3.text = "Extra time will be chargeable."
        rule4.text = "Fare may change if route or location changes."
        rule5.text = "Parking charges to be paid by customer."
        rule6.text = "Fare includes toll and permit charges, if any."
        rule7.text = "We don't allow overloading."

        // BOOK now → no data passed via Intent
        bookButton.setOnClickListener {
            startActivity(Intent(this, WaitingForApprovalActivity::class.java))
            finish()
        }
    }
}
