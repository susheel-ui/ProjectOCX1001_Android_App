package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.DriverContactResponse
import kotlinx.coroutines.*
import retrofit2.awaitResponse

class WaitingForApprovalActivity : AppCompatActivity() {

    private lateinit var pickupInfo: TextView
    private lateinit var dropInfo: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var progressBar: ProgressBar

    private var rideId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.waiting_for_approval)

        pickupInfo = findViewById(R.id.pickupInfo)
        dropInfo = findViewById(R.id.dropInfo)
        vehicleInfo = findViewById(R.id.vehicleInfo)
        progressBar = findViewById(R.id.progressBar)

        rideId = LocalStorage.getActiveRideId(this)

        pickupInfo.text = "Searching nearby driver..."
        dropInfo.text = "Please wait"
        vehicleInfo.text = "Matching driver"

        pollDriverApproval()
    }

    private fun pollDriverApproval() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(4000)

                try {
                    val response = ApiClient.api
                        .getDriverContact(rideId)
                        .awaitResponse()

                    if (response.isSuccessful && response.body() != null) {

                        val driver = response.body()!!

                        // ✅ SAVE DRIVER ID
                        LocalStorage.saveActiveDriverId(
                            this@WaitingForApprovalActivity,
                            driver.driverId
                        )

                        withContext(Dispatchers.Main) {
                            val intent = Intent(
                                this@WaitingForApprovalActivity,
                                DriverDetailsActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                        break
                    }

                } catch (_: Exception) {
                    // driver not assigned yet → keep polling
                }
            }
        }
    }
}
