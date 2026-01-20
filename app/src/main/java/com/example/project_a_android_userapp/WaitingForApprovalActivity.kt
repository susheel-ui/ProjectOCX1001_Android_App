package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.ApiClient
import kotlinx.coroutines.*
import retrofit2.awaitResponse

class WaitingForApprovalActivity : AppCompatActivity() {

    private lateinit var pickupInfo: TextView
    private lateinit var dropInfo: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cancelTrip: TextView

    private var rideId: Long = -1L
    private var pollingJob: Job? = null

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val POLLING_INTERVAL = 4_000L
        private const val AUTO_CANCEL_TIME = 180_000L // 3 minute
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.waiting_for_approval)

        pickupInfo = findViewById(R.id.pickupInfo)
        dropInfo = findViewById(R.id.dropInfo)
        vehicleInfo = findViewById(R.id.vehicleInfo)
        progressBar = findViewById(R.id.progressBar)
        cancelTrip = findViewById(R.id.cancelTrip)

        rideId = LocalStorage.getActiveRideId(this)

        if (rideId == -1L) {
            Toast.makeText(this, "Invalid ride", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pickupInfo.text = "Searching nearby driver..."
        dropInfo.text = "Please wait"
        vehicleInfo.text = "Matching driver"

        startPolling()

        cancelTrip.setOnClickListener {
            manualCancel()
        }
    }

    // =====================================================
    // 🔄 POLLING WITH 1-MIN TIMEOUT
    // =====================================================
    private fun startPolling() {

        pollingJob = activityScope.launch {

            val startTime = System.currentTimeMillis()

            while (isActive) {

                if (System.currentTimeMillis() - startTime >= AUTO_CANCEL_TIME) {
                    autoCancelWithRetry()
                    break
                }

                delay(POLLING_INTERVAL)

                try {
                    val response = withContext(Dispatchers.IO) {
                        ApiClient.api.getDriverContact(rideId).awaitResponse()
                    }

                    if (response.isSuccessful && response.body() != null) {

                        val driver = response.body()!!

                        LocalStorage.saveActiveDriverId(
                            this@WaitingForApprovalActivity,
                            driver.driverId
                        )

                        startActivity(
                            Intent(
                                this@WaitingForApprovalActivity,
                                DriverDetailsActivity::class.java
                            )
                        )
                        finish()
                        break
                    }

                } catch (e: CancellationException) {
                    break
                } catch (_: Exception) {
                    // Ignore & continue polling
                }
            }
        }
    }

    // =====================================================
    // ❌ MANUAL CANCEL
    // =====================================================
    private fun manualCancel() {
        pollingJob?.cancel()
        cancelRide {
            redirectHome()
        }
    }

    // =====================================================
    // ⏰ AUTO CANCEL + RETRY OPTION
    // =====================================================
    private fun autoCancelWithRetry() {
        pollingJob?.cancel()
        cancelRide {
            showRetryDialog()
        }
    }

    // =====================================================
    // 🚫 CANCEL API
    // =====================================================
    private fun cancelRide(onDone: () -> Unit) {

        val rideId = LocalStorage.getActiveRideId(this)
        val userId = LocalStorage.getUserId(this)
        val token = LocalStorage.getToken(this)

        activityScope.launch(Dispatchers.IO) {
            try {
                if (rideId != -1L && userId != -1 && !token.isNullOrEmpty()) {
                    ApiClient.api.cancelRide(
                        rideId = rideId,
                        userId = userId.toLong(),
                        authHeader = "Bearer $token"
                    ).awaitResponse()
                }
            } catch (_: Exception) {
                // Ignore
            }

            withContext(Dispatchers.Main) {
                clearRideData()
                onDone()
            }
        }
    }

    // =====================================================
    // 🔁 RETRY DIALOG
    // =====================================================
    private fun showRetryDialog() {

        AlertDialog.Builder(this)
            .setTitle("No driver found")
            .setMessage("No driver accepted your request.\nWould you like to try again?")
            .setCancelable(false)
            .setPositiveButton("Try Again") { _, _ ->
                retryRide()
            }
            .setNegativeButton("Go Back") { _, _ ->
                redirectHome()
            }
            .show()
    }

    // =====================================================
    // 🔁 RETRY LOGIC
    // =====================================================
    private fun retryRide() {
        startActivity(
            Intent(this, FinalFareActivity::class.java)
        )
        finish()
    }

    // =====================================================
    // 🧹 CLEAR LOCAL STATE
    // =====================================================
    private fun clearRideData() {
        LocalStorage.saveActiveRideId(this, -1L)
        LocalStorage.saveActiveDriverId(this, -1L)
    }

    // =====================================================
    // 🏠 HOME
    // =====================================================
    private fun redirectHome() {
        startActivity(
            Intent(this, FinalFareActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        activityScope.cancel()
        super.onDestroy()
    }
}
