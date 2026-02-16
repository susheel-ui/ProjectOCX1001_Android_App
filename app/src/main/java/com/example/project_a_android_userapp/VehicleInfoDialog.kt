package com.example.project_a_android_userapp

import android.app.Activity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.project_a_android_userapp.R

object VehicleInfoDialog {

    // Define a data class to hold vehicle info
    private data class VehicleInfo(
        val title: String,
        val desc: String,
        val imageRes: Int
    )

    // Map vehicle type to info
    private val vehicleData = mapOf(
        "TWO_WHEELER_EV" to VehicleInfo(
            "2 Wheeler EV", "20 kg : 40cm × 40cm × 40cm", R.drawable.bikeinfo
        ),
        "TWO_WHEELER_PETROL" to VehicleInfo(
            "2 Wheeler Petrol", "20 kg : 40cm × 40cm × 40cm", R.drawable.bikeinfo
        ),
        "THREE_WHEELER_EV" to VehicleInfo(
            "3 Wheeler EV", "500 kg : 6 ft × 4.6 ft × 5 ft", R.drawable.taxiinfo
        ),
        "THREE_WHEELER_PETROL" to VehicleInfo(
            "3 Wheeler Petrol", "500 kg : 6 ft × 4.6 ft × 5 ft", R.drawable.taxiinfo
        ),
        "THREE_WHEELER_CNG" to VehicleInfo(
            "3 Wheeler CNG", "500 kg : 6 ft × 4.6 ft × 5 ft", R.drawable.taxiinfo
        ),
        "FOUR_WHEELER_EV" to VehicleInfo(
            "4 Wheeler EV", "1700 kg : 10 ft × 5.5 ft × 5.5 ft", R.drawable.truckinfo
        ),
        "FOUR_WHEELER_PETROL" to VehicleInfo(
            "4 Wheeler Petrol", "1700 kg : 10 ft × 5.5 ft × 5.5 ft", R.drawable.truckinfo
        ),
        "FOUR_WHEELER_CNG" to VehicleInfo(
            "4 Wheeler CNG", "1700 kg : 10 ft × 5.5 ft × 5.5 ft", R.drawable.truckinfo
        )
    )

    fun show(context: Activity, vehicleType: String, fare: Double) {
        val info = vehicleData[vehicleType] ?: return

        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_vehicle_info, null)
        dialog.setContentView(view)

        // Optional: make full width
        (view.parent as? android.view.View)?.layoutParams?.width =
            android.view.ViewGroup.LayoutParams.MATCH_PARENT

        // Bind views
        val image = view.findViewById<ImageView>(R.id.vehicleImage)
        val title = view.findViewById<TextView>(R.id.vehicleTitle)
        val desc = view.findViewById<TextView>(R.id.vehicleDesc)
        val fareText = view.findViewById<TextView>(R.id.vehicleFare)
        val doneBtn = view.findViewById<Button>(R.id.doneBtn)

        // Set values
        image.setImageResource(info.imageRes)
        title.text = info.title
        desc.text = info.desc
        fareText.text = "Estimated Fare: ₹${fare.toInt()}"

        doneBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
