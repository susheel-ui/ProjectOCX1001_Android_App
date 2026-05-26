package com.zarkit.zarkit_user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zarkit.zarkit_user.api.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinalFareActivity : BaseActivity() {

    private lateinit var btnChange: TextView
    private lateinit var goodsTypeText: TextView
    private var savedGstNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_final_fare)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Rules — item_rule ke andar ruleText TextView hai ──
        findViewById<LinearLayout>(R.id.rule1).findViewById<TextView>(R.id.ruleText).text = "No fragile items allowed"
        findViewById<LinearLayout>(R.id.rule2).findViewById<TextView>(R.id.ruleText).text = "Maximum weight limit depends on the vehicle"
        findViewById<LinearLayout>(R.id.rule3).findViewById<TextView>(R.id.ruleText).text = "Ensure proper packaging"
        findViewById<LinearLayout>(R.id.rule4).findViewById<TextView>(R.id.ruleText).text = "Delivery within 24 hours"
        findViewById<LinearLayout>(R.id.rule5).findViewById<TextView>(R.id.ruleText).text = "Driver not responsible for loss of valuables"
        findViewById<LinearLayout>(R.id.rule6).findViewById<TextView>(R.id.ruleText).text = "Payment must be made before pickup"
        findViewById<LinearLayout>(R.id.rule7).findViewById<TextView>(R.id.ruleText).text = "Follow all traffic rules"

        val addressDetails    = findViewById<TextView>(R.id.addressDetails)
        val btnBack           = findViewById<ImageView>(R.id.btnBack)
        val vehicleImage      = findViewById<ImageView>(R.id.vehicleImage)
        val vehicleName       = findViewById<TextView>(R.id.vehicleName)
        val finalFareText     = findViewById<TextView>(R.id.finalFareText)
        val paymentFare       = findViewById<TextView>(R.id.paymentFare)
        val fareDetailsLayout = findViewById<LinearLayout>(R.id.fareDetailsLayout)
        val bookButton        = findViewById<Button>(R.id.bookNowButton)
        val btnAddGstTop      = findViewById<TextView>(R.id.btnAddGstTop)
        val btnViewList       = findViewById<TextView>(R.id.btnViewList)

        goodsTypeText = findViewById(R.id.goodsType)
        btnChange     = findViewById(R.id.btnChange)

        // ── Back button ──
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // ── Address Details ──
        addressDetails.setOnClickListener { openAddressDetailsPopup() }

        // ── GST button ──
        btnAddGstTop.setOnClickListener { openGstDetailsBottomSheet(btnAddGstTop) }

        // ── Restricted items ──
        btnViewList.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_restricted_items, null)
            sheetView.findViewById<TextView>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
            sheetView.findViewById<Button>(R.id.btnOkUnderstood).setOnClickListener { dialog.dismiss() }
            dialog.setContentView(sheetView)
            dialog.show()
        }

        // ── Goods type default ──
        if (LocalStorage.getGoodsType(this).isEmpty()) {
            LocalStorage.saveFareDetails(
                this,
                vehicle   = LocalStorage.getSelectedVehicle(this),
                fare      = LocalStorage.getFinalFare(this),
                goodsType = "General • Loose"
            )
        }
        goodsTypeText.text = LocalStorage.getGoodsType(this)
        btnChange.setOnClickListener { openGoodsTypeBottomSheet() }

        // ── Fare display ──
        val totalFare = LocalStorage.getFinalFare(this)
        val baseFare  = totalFare / 1.18
        val gstAmount = totalFare - baseFare

        finalFareText.text = "₹${String.format("%.2f", totalFare)}"
        paymentFare.text   = "₹${String.format("%.2f", totalFare)}"

        // ── Price breakdown bottom sheet ──
        fareDetailsLayout.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view   = layoutInflater.inflate(R.layout.bottom_sheet_price_breakdown, null)
            view.findViewById<TextView>(R.id.tvBaseFare).text  = "₹${String.format("%.2f", baseFare)}"
            view.findViewById<TextView>(R.id.tvGstAmount).text = "₹${String.format("%.2f", gstAmount)}"
            view.findViewById<TextView>(R.id.tvTotalFare).text = "₹${String.format("%.2f", totalFare)}"
            view.findViewById<Button>(R.id.btnClosePriceBreakdown).setOnClickListener { dialog.dismiss() }
            dialog.setContentView(view)
            dialog.show()
        }

        // ── Vehicle image ──
        when (LocalStorage.getSelectedVehicle(this)) {
            "TWO_WHEELER_EV", "TWO_WHEELER_PETROL" -> {
                vehicleImage.setImageResource(R.drawable.v2w)
                vehicleName.text = "2 Wheeler"
            }
            "THREE_WHEELER_EV", "THREE_WHEELER_PETROL", "THREE_WHEELER_CNG" -> {
                vehicleImage.setImageResource(R.drawable.v3w)
                vehicleName.text = "3 Wheeler"
            }
            "FOUR_WHEELER_CNG", "FOUR_WHEELER_EV", "FOUR_WHEELER_PETROL" -> {
                vehicleImage.setImageResource(R.drawable.v4w)
                vehicleName.text = "Truck"
            }
            else -> {
                vehicleImage.setImageResource(R.drawable.zarkitgroup)
                vehicleName.text = "Vehicle"
            }
        }

        // ── Book button ──
        bookButton.setOnClickListener {

            val activeRideId = LocalStorage.getActiveRideId(this)
            if (activeRideId > 0L) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Active Ride Found")
                    .setMessage("Please complete your current ride first before booking a new one.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
                return@setOnClickListener
            }

            bookButton.isEnabled = false

            val createRideRequest = CreateRideRequest(
                pickupLat     = LocalStorage.getPickupLat(this),
                pickupLon     = LocalStorage.getPickupLng(this),
                pickupAddress = LocalStorage.getPickupAddress(this),
                dropLat       = LocalStorage.getDropLat(this),
                dropLon       = LocalStorage.getDropLng(this),
                dropAddress   = LocalStorage.getDropAddress(this),
                distanceText  = LocalStorage.getDistanceText(this),
                durationText  = LocalStorage.getDurationText(this),
                distanceValue = LocalStorage.getDistanceValue(this),
                durationValue = LocalStorage.getDurationValue(this),
                senderHouse   = LocalStorage.getSenderHouse(this),
                senderName    = LocalStorage.getSenderName(this),
                senderPhone   = LocalStorage.getSenderPhone(this),
                senderType    = LocalStorage.getSenderType(this),
                receiverHouse = LocalStorage.getReceiverHouse(this),
                receiverName  = LocalStorage.getReceiverName(this),
                receiverPhone = LocalStorage.getReceiverPhone(this),
                receiverType  = LocalStorage.getReceiverType(this),
                vehicleInfo   = LocalStorage.getSelectedVehicle(this),
                finalFare     = LocalStorage.getFinalFare(this)
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
                            LocalStorage.saveActiveRideId(this@FinalFareActivity, ride.rideId)
                            sendNotification(ride.rideId)
                            startActivity(Intent(this@FinalFareActivity, WaitingForApprovalActivity::class.java))
                            finish()
                        } else {
                            bookButton.isEnabled = true
                            Toast.makeText(this@FinalFareActivity, "Ride creation failed", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateRideResponse>, t: Throwable) {
                        bookButton.isEnabled = true
                        Toast.makeText(this@FinalFareActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    // ================= GST BOTTOM SHEET =================

    private fun openGstDetailsBottomSheet(btnAddGstTop: TextView) {
        val dialog = BottomSheetDialog(this)
        val view   = layoutInflater.inflate(R.layout.bottom_sheet_gst_details, null)

        val etGstNumber = view.findViewById<EditText>(R.id.etGstNumber)
        val btnSaveGst  = view.findViewById<Button>(R.id.btnSaveGst)
        val btnCloseGst = view.findViewById<Button>(R.id.btnCloseGst)

        if (savedGstNumber.isNotEmpty()) {
            etGstNumber.setText(savedGstNumber)
            btnSaveGst.text = "Update GST"
        } else {
            btnSaveGst.text = "Save GST"
        }

        btnSaveGst.setOnClickListener {
            val gst = etGstNumber.text.toString().trim().uppercase()
            if (gst.isEmpty()) {
                etGstNumber.error = "Enter GST number"
                return@setOnClickListener
            }
            val userId = LocalStorage.getUserId(this)
            if (userId == -1) {
                Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnSaveGst.isEnabled = false
            ApiClient.api.updateUserPartial(userId, mapOf("gstin" to gst))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        btnSaveGst.isEnabled = true
                        if (response.isSuccessful) {
                            savedGstNumber = gst
                            btnAddGstTop.text = "✓ GST Added"
                            btnAddGstTop.setBackgroundResource(R.drawable.bg_gst_added)
                            btnAddGstTop.setTextColor(android.graphics.Color.WHITE)
                            Toast.makeText(this@FinalFareActivity, "GST saved successfully", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@FinalFareActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        btnSaveGst.isEnabled = true
                        Toast.makeText(this@FinalFareActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        btnCloseGst.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }

    // ================= ADDRESS POPUP =================

    private fun openAddressDetailsPopup() {
        val dialog = BottomSheetDialog(this)
        val view   = layoutInflater.inflate(R.layout.bottom_sheet_location_details, null)
        view.findViewById<TextView>(R.id.tvPickupAddress).text = LocalStorage.getPickupAddress(this)
        view.findViewById<TextView>(R.id.tvDropAddress).text   = LocalStorage.getDropAddress(this)
        view.findViewById<Button>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        dialog.setContentView(view)
        dialog.show()
    }

    // ================= GOODS TYPE =================

    private fun openGoodsTypeBottomSheet() {
        val dialog    = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_goods_type, null)

        val options = mapOf(
            R.id.optionGeneral     to "General • Loose",
            R.id.optionFragile     to "Fragile • Glass",
            R.id.optionElectronics to "Electronics",
            R.id.optionLogistics   to "Logistics Service Providers",
            R.id.optionMachines    to "Machines / Equipments / Spare Parts",
            R.id.optionPharma      to "Pharmaceutical / Healthcare Products",
            R.id.optionPlastic     to "Plastic Products",
            R.id.optionRubber      to "Rubber Products",
            R.id.optionTextile     to "Textiles / Garments / Fashion / Accessories",
            R.id.optionTimber      to "Timbers / Plywoods / Papers",
            R.id.optionBooks       to "Books / Stationary / Gifts / Toys"
        )

        options.forEach { (id, value) ->
            sheetView.findViewById<TextView>(id).setOnClickListener {
                updateGoods(value, dialog)
            }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun updateGoods(value: String, dialog: BottomSheetDialog) {
        LocalStorage.saveFareDetails(
            this,
            vehicle   = LocalStorage.getSelectedVehicle(this),
            fare      = LocalStorage.getFinalFare(this),
            goodsType = value
        )
        goodsTypeText.text = value.trim().split(" ")[0]
        dialog.dismiss()
    }

    // ================= NOTIFICATION =================

    private fun sendNotification(rideId: Long) {
        val notificationRequest = RideNotificationRequest(
            rideId   = rideId,
            message  = "New Ride Request",
            fare     = LocalStorage.getFinalFare(this),
            vehicle  = mapVehicleForBackend(LocalStorage.getSelectedVehicle(this)),
            pickup   = LocalStorage.getPickupAddress(this),
            drop     = LocalStorage.getDropAddress(this),
            distance = LocalStorage.getDistanceText(this)
        )
        ApiClient.api.sendRideNotification(notificationRequest)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {}
                override fun onFailure(call: Call<String>, t: Throwable) {}
            })
    }

    private fun mapVehicleForBackend(vehicle: String): String {
        return when (vehicle) {
            "Bike"   -> "BIKE"
            "Loader" -> "THREE_WHEELER"
            "Truck"  -> "FOUR_WHEELER_EV"
            else     -> vehicle.uppercase()
        }
    }
}