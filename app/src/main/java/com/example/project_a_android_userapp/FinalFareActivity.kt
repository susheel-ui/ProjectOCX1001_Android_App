package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinalFareActivity : AppCompatActivity() {

    private lateinit var vm: LocationViewModel
    private var selectedGoods = "General • Loose"

    // views reused
    private lateinit var goodsTypeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_fare)

        vm = (application as MyApp).vm

        val vehicleImage = findViewById<ImageView>(R.id.vehicleImage)
        val finalFareText = findViewById<TextView>(R.id.finalFareText)
        val paymentFare = findViewById<TextView>(R.id.paymentFare)
        val bookButton = findViewById<Button>(R.id.bookNowButton)

        goodsTypeText = findViewById(R.id.goodsType)
        val changeGoods = findViewById<TextView>(R.id.btnChangeGoods)

        val readBefore = findViewById<TextView>(R.id.readBeforeBooking)
        val rulesCard = findViewById<LinearLayout>(R.id.rulesCard)

        // ================= PRICE (NO GST) =================
        val fare = vm.finalFare
        val formattedFare = String.format("%.2f", fare)

        finalFareText.text = "₹$formattedFare"
        paymentFare.text = "₹$formattedFare"
        goodsTypeText.text = selectedGoods

        // ================= VEHICLE IMAGE =================
        when (vm.selectedVehicle) {
            "Bike" -> vehicleImage.setImageResource(R.drawable.mini_3w)
            "Loader" -> vehicleImage.setImageResource(R.drawable.loader)
            "Truck" -> vehicleImage.setImageResource(R.drawable.truck)
        }

        // ================= CHANGE GOODS =================
        changeGoods.setOnClickListener { showGoodsBottomSheet() }

        // ================= READ BEFORE BOOKING =================
        rulesCard.visibility = View.GONE
        readBefore.setOnClickListener {
            rulesCard.visibility =
                if (rulesCard.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        rulesCard.requestLayout()

        setRules()

        // ================= BOOK NOW =================
        bookButton.setOnClickListener {
            bookButton.isEnabled = false

            val request = CreateRideRequest(
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

            ApiClient.api.createRide(request)
                .enqueue(object : Callback<CreateRideResponse> {

                    override fun onResponse(
                        call: Call<CreateRideResponse>,
                        response: Response<CreateRideResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val ride = response.body()!!
                            LocalStorage.saveActiveRideId(
                                this@FinalFareActivity,
                                ride.rideId
                            )
                            sendNotification(ride.rideId)

                            startActivity(
                                Intent(
                                    this@FinalFareActivity,
                                    WaitingForApprovalActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            bookButton.isEnabled = true
                            toast("Ride creation failed")
                        }
                    }

                    override fun onFailure(call: Call<CreateRideResponse>, t: Throwable) {
                        bookButton.isEnabled = true
                        toast("Network error")
                    }
                })
        }
    }

    // ================= GOODS BOTTOM SHEET =================
    private fun showGoodsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_goods_type, null)
        dialog.setContentView(view)

        var selected = selectedGoods

        fun bind(id: Int, value: String) {
            view.findViewById<LinearLayout>(id).setOnClickListener {
                selected = value
            }
        }

        bind(R.id.optionPharma, "Pharmaceutical / Healthcare")
        bind(R.id.optionPlastic, "Plastic Products")
        bind(R.id.optionRubber, "Rubber Products")
        bind(R.id.optionTextile, "Textiles / Garments")
        bind(R.id.optionTimber, "Timbers / Plywoods / Papers")
        bind(R.id.optionBooks, "Books / Stationery / Gifts / Toys")

        view.findViewById<Button>(R.id.btnConfirmGoods).setOnClickListener {
            selectedGoods = selected
            goodsTypeText.text = selectedGoods
            dialog.dismiss()
        }

        dialog.show()
    }

    // ================= RULES =================
    private fun setRules() {
        findViewById<TextView>(R.id.rule1).text = "• Package must be sealed properly"
        findViewById<TextView>(R.id.rule2).text = "• No restricted or illegal items"
        findViewById<TextView>(R.id.rule3).text = "• Correct address is mandatory"
        findViewById<TextView>(R.id.rule4).text = "• Extra charges for waiting time"
        findViewById<TextView>(R.id.rule5).text = "• Driver may refuse unsafe goods"
        findViewById<TextView>(R.id.rule6).text = "• Cancellation charges may apply"
        findViewById<TextView>(R.id.rule7).text = "• Support available 24x7"
    }

    // ================= NOTIFICATION =================
    private fun sendNotification(rideId: Long) {
        val req = RideNotificationRequest(
            rideId = rideId,
            message = "New Ride Request",
            fare = vm.finalFare,
            vehicle = mapVehicle(vm.selectedVehicle),
            pickup = vm.pickupAddress,
            drop = vm.dropAddress,
            distance = vm.distanceText
        )

        ApiClient.api.sendRideNotification(req)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {}
                override fun onFailure(call: Call<String>, t: Throwable) {}
            })
    }

    private fun mapVehicle(vehicle: String): String =
        when (vehicle) {
            "Bike" -> "BIKE"
            "Loader" -> "THREE_WHEELER"
            "Truck" -> "FOUR_WHEELER_EV"
            else -> vehicle.uppercase()
        }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
