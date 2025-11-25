package com.example.project_a_android_userapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.databinding.ActivityDriverDetailsBinding

class DriverDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vehicle = intent.getStringExtra("vehicle") ?: "Bike"

        binding.driverNameText.text = "Driver: Ravi Kumar"
        binding.driverPhoneText.text = "Phone: +91-9876543210"
        binding.vehicleText.text = "Vehicle: $vehicle"
    }
}
