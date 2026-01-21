package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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

    // ===== Goods Type UI =====
    private lateinit var btnChange: TextView
    private lateinit var goodsTypeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        vm = (application as MyApp).vm

        val rule1 = findViewById<TextView>(R.id.rule1)
        val rule2 = findViewById<TextView>(R.id.rule2)
        val rule3 = findViewById<TextView>(R.id.rule3)
        val rule4 = findViewById<TextView>(R.id.rule4)
        val rule5 = findViewById<TextView>(R.id.rule5)
        val rule6 = findViewById<TextView>(R.id.rule6)
        val rule7 = findViewById<TextView>(R.id.rule7)

        rule1.text = "\u2022 No fragile items allowed"
        rule2.text = "\u2022 Maximum weight limit depends on the vehicle."
        rule3.text = "\u2022 Ensure proper packaging"
        rule4.text = "\u2022 Delivery within 24 hours"
        rule5.text = "\u2022 Driver not responsible for loss of valuables"
        rule6.text = "\u2022 Payment must be made before pickup"
        rule7.text = "\u2022 Follow all traffic rules"


        val vehicleImage = findViewById<ImageView>(R.id.vehicleImage)
        val finalFareText = findViewById<TextView>(R.id.finalFareText)
        val paymentFare = findViewById<TextView>(R.id.paymentFare)
        val bookButton = findViewById<Button>(R.id.bookNowButton)

        val btnViewList = findViewById<TextView>(R.id.btnViewList)

        btnViewList.setOnClickListener {

            val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
            val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_restricted_items, null)

            dialog.setContentView(sheetView)
            dialog.show()
        }


        // ===== Goods Type Views =====
        goodsTypeText = findViewById(R.id.goodsType)
        btnChange = findViewById(R.id.btnChange)

        // Default Goods Type
        if (vm.goodsType.isEmpty()) {
            vm.goodsType = "General • Loose"
        }

        goodsTypeText.text = vm.goodsType

        btnChange.setOnClickListener {
            openGoodsTypeBottomSheet()
        }

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

    // ================= GOODS TYPE BOTTOM SHEET =================

    private fun openGoodsTypeBottomSheet() {

        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_goods_type, null)

        val optionGeneral = sheetView.findViewById<TextView>(R.id.optionGeneral)
        val optionFragile = sheetView.findViewById<TextView>(R.id.optionFragile)
        val optionElectronics = sheetView.findViewById<TextView>(R.id.optionElectronics)
        val optionLogistics = sheetView.findViewById<TextView>(R.id.optionLogistics)
        val optionMachines = sheetView.findViewById<TextView>(R.id.optionMachines)
        val optionPharma = sheetView.findViewById<TextView>(R.id.optionPharma)
        val optionPlastic = sheetView.findViewById<TextView>(R.id.optionPlastic)
        val optionRubber = sheetView.findViewById<TextView>(R.id.optionRubber)
        val optionTextile = sheetView.findViewById<TextView>(R.id.optionTextile)
        val optionTimber = sheetView.findViewById<TextView>(R.id.optionTimber)
        val optionBooks = sheetView.findViewById<TextView>(R.id.optionBooks)

        optionGeneral.setOnClickListener { updateGoods("General • Loose", dialog) }
        optionFragile.setOnClickListener { updateGoods("Fragile • Glass", dialog) }
        optionElectronics.setOnClickListener { updateGoods("Electronics", dialog) }
        optionLogistics.setOnClickListener { updateGoods("Logistics Service Providers", dialog) }
        optionMachines.setOnClickListener { updateGoods("Machines / Equipments / Spare Parts", dialog) }
        optionPharma.setOnClickListener { updateGoods("Pharmaceutical / Healthcare Products", dialog) }
        optionPlastic.setOnClickListener { updateGoods("Plastic Products", dialog) }
        optionRubber.setOnClickListener { updateGoods("Rubber Products", dialog) }
        optionTextile.setOnClickListener { updateGoods("Textiles / Garments / Fashion / Accessories", dialog) }
        optionTimber.setOnClickListener { updateGoods("Timbers / Plywoods / Papers", dialog) }
        optionBooks.setOnClickListener { updateGoods("Books / Stationary / Gifts / Toys", dialog) }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun updateGoods(value: String, dialog: BottomSheetDialog) {
        // Store full value for backend/API
        vm.goodsType = value

        // Display only the first word on screen
        val firstWord = value.trim().split(" ")[0]
        goodsTypeText.text = firstWord

        dialog.dismiss()
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
