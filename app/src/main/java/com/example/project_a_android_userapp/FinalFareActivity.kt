package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinalFareActivity : AppCompatActivity() {

    private lateinit var vm: LocationViewModel

    private lateinit var selectedGoods: String

    private fun showGoodsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_goods_type, null)
        dialog.setContentView(view)

        fun selectGoods(name: String) {
            selectedGoods = name
        }

        view.findViewById<LinearLayout>(R.id.optionPharma)
            .setOnClickListener { selectGoods("Pharmaceutical / Healthcare") }

        view.findViewById<LinearLayout>(R.id.optionPlastic)
            .setOnClickListener { selectGoods("Plastic Products") }

        view.findViewById<LinearLayout>(R.id.optionRubber)
            .setOnClickListener { selectGoods("Rubber Products") }

        view.findViewById<LinearLayout>(R.id.optionTextile)
            .setOnClickListener { selectGoods("Textiles / Garments") }

        view.findViewById<LinearLayout>(R.id.optionTimber)
            .setOnClickListener { selectGoods("Timbers / Plywoods / Papers") }

        view.findViewById<LinearLayout>(R.id.optionBooks)
            .setOnClickListener { selectGoods("Books / Stationery / Gifts / Toys") }

        view.findViewById<Button>(R.id.btnConfirmGoods).setOnClickListener {
            findViewById<TextView>(R.id.goodsType).text = selectedGoods
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        vm = (application as MyApp).vm

        val vehicleImage = findViewById<ImageView>(R.id.vehicleImage)
        val finalFareText = findViewById<TextView>(R.id.finalFareText)
        val paymentFare = findViewById<TextView>(R.id.paymentFare)
        val bookButton = findViewById<Button>(R.id.bookNowButton)

        // ================= UI =================

        val originalFare = vm.finalFare
        val gst = originalFare * 0.18
        val finalFareWithGST = originalFare + gst

        paymentFare.text = "₹${String.format("%.2f", finalFareWithGST)}"
        finalFareText.text = "₹${String.format("%.2f", originalFare)}"

        when (vm.selectedVehicle) {
            "Bike" -> vehicleImage.setImageResource(R.drawable.mini_3w)
            "Loader" -> vehicleImage.setImageResource(R.drawable.loader)
            "Truck" -> vehicleImage.setImageResource(R.drawable.truck)
        }

        // ================= BOOK NOW =================

        bookButton.setOnClickListener {

            bookButton.isEnabled = false

            val createRideRequest = CreateRideRequest(
                pickupLat = vm.pickupLat,
                pickupLon = vm.pickupLon,
                pickupAddress = vm.pickupAddress,

                dropLat = vm.dropLat,
                dropLon = vm.dropLon,
                dropAddress = vm.dropAddress,

                distanceText = vm.distanceText,
                durationText = vm.durationText,
                distanceValue = vm.distanceValue,
                durationValue = vm.durationValue,

                senderHouse = vm.senderHouse,
                senderName = vm.senderName,
                senderPhone = vm.senderPhone,
                senderType = vm.senderType,

                receiverHouse = vm.receiverHouse,
                receiverName = vm.receiverName,
                receiverPhone = vm.receiverPhone,
                receiverType = vm.receiverType,

                vehicleInfo = vm.selectedVehicle,
                finalFare = vm.finalFare
            )

            Toast.makeText(this, "Creating ride...", Toast.LENGTH_SHORT).show()

            ApiClient.api.createRide(createRideRequest)
                .enqueue(object : Callback<CreateRideResponse> {

                    override fun onResponse(
                        call: Call<CreateRideResponse>,
                        response: Response<CreateRideResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {

                            val ride = response.body()!!

                            // ✅ STORE rideId locally
                            LocalStorage.saveActiveRideId(
                                this@FinalFareActivity,
                                ride.rideId
                            )

                            // ✅ SEND NOTIFICATION
                            sendNotification(ride.rideId)

                            // ✅ MOVE TO WAITING SCREEN
                            startActivity(
                                Intent(
                                    this@FinalFareActivity,
                                    WaitingForApprovalActivity::class.java
                                )
                            )
                            finish()

                        } else {
                            bookButton.isEnabled = true
                            Toast.makeText(
                                this@FinalFareActivity,
                                "Ride creation failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateRideResponse>, t: Throwable) {
                        bookButton.isEnabled = true
                        Toast.makeText(
                            this@FinalFareActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    // ================= SEND NOTIFICATION =================

    private fun sendNotification(rideId: Long) {

        val notificationRequest = RideNotificationRequest(
            rideId = rideId,
            message = "New Ride Request",
            fare = vm.finalFare,
            vehicle = mapVehicleForBackend(vm.selectedVehicle),
            pickup = vm.pickupAddress,
            drop = vm.dropAddress,
            distance = vm.distanceText
        )

        ApiClient.api.sendRideNotification(notificationRequest)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {}
                override fun onFailure(call: Call<String>, t: Throwable) {}
            })
    }

    // ================= VEHICLE MAPPING =================

    private fun mapVehicleForBackend(vehicle: String): String {
        return when (vehicle) {
            "Bike" -> "BIKE"
            "Loader" -> "THREE_WHEELER"
            "Truck" -> "FOUR_WHEELER_EV"
            else -> vehicle.uppercase()
        }
    }
}
