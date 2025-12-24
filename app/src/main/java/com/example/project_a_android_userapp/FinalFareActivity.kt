package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.RideRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinalFareActivity : AppCompatActivity() {

    private lateinit var vm: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        vm = (application as MyApp).vm

        val vehicle = vm.selectedVehicle
        val originalFare = vm.finalFare

        val gst = originalFare * 0.18
        val finalFareWithGST = originalFare + gst

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

        originalFareText.text = "₹${String.format("%.2f", finalFareWithGST)}"
        gstFareText.text = "₹${String.format("%.2f", originalFare)}"

        when (vehicle) {
            "Bike" -> vehicleImage.setImageResource(R.drawable.mini_3w)
            "Loader" -> vehicleImage.setImageResource(R.drawable.loader)
            "Truck" -> vehicleImage.setImageResource(R.drawable.truck)
        }

        rule1.text = "Fare doesn't include labour charges for loading & unloading."
        rule2.text = "Fare includes 25 mins free loading/unloading time."
        rule3.text = "Extra time will be chargeable."
        rule4.text = "Fare may change if route or location changes."
        rule5.text = "Parking charges to be paid by customer."
        rule6.text = "Fare includes toll and permit charges, if any."
        rule7.text = "We don't allow overloading."

        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        //  BOOK NOW → SEND NOTIFICATION → THEN MOVE SCREEN
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        bookButton.setOnClickListener {

            val body = RideRequestBody(
                message = "New Ride Request",
                fare = vm.finalFare,
                vehicle = vm.selectedVehicle ?: "",
                pickup = vm.pickupAddress ?: "",
                drop = vm.dropAddress ?: "",
                distance = vm.distanceText ?: "0 km"
            )


            Toast.makeText(this, "Sending request...", Toast.LENGTH_SHORT).show()

            ApiClient.api.sendRideRequest(body).enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FinalFareActivity, "Request Sent!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@FinalFareActivity, WaitingForApprovalActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@FinalFareActivity, "Failed to send. Try again.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@FinalFareActivity, "Network Error", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}





