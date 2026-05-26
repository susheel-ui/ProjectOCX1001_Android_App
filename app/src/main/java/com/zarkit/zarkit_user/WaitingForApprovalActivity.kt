package com.zarkit.zarkit_user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zarkit.zarkit_user.api.ApiClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import retrofit2.awaitResponse

class WaitingForApprovalActivity : BaseActivity() {

    private lateinit var pickupInfo: TextView
    private lateinit var dropInfo: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cancelTrip: TextView
    private lateinit var cash: TextView

    private var rideId: Long = -1L
    private var pollingJob: Job? = null

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val POLLING_INTERVAL = 4_000L
        private const val AUTO_CANCEL_TIME = 180_000L // 3 minutes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.waiting_for_approval)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pickupInfo  = findViewById(R.id.pickupInfo)
        dropInfo    = findViewById(R.id.dropInfo)
        vehicleInfo = findViewById(R.id.vehicleInfo)
        progressBar = findViewById(R.id.progressBar)
        cancelTrip  = findViewById(R.id.cancelTrip)
        cash        = findViewById(R.id.cash)

        rideId = LocalStorage.getActiveRideId(this)

        if (rideId == -1L) {
            Toast.makeText(this, "Invalid ride", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<LinearLayout>(R.id.btn_support).setOnClickListener {
            openSupportPopup()
        }

        findViewById<TextView>(R.id.btn_view_details).setOnClickListener {
            openBookingDetailsPopup()
        }

        // ── Show data from LocalStorage ──
        val pickupAddress = LocalStorage.getPickupAddress(this)
        val dropAddress   = LocalStorage.getDropAddress(this)

        pickupInfo.text  = pickupAddress.trim().split(" ").take(3).joinToString(" ")
        dropInfo.text    = dropAddress.trim().split(" ").take(3).joinToString(" ")
        vehicleInfo.text = LocalStorage.getSelectedVehicle(this)
        cash.text        = "₹${String.format("%.2f", LocalStorage.getFinalFare(this))}"

        startPolling()

        cancelTrip.setOnClickListener { manualCancel() }
    }

    // =====================================================
    // POLLING WITH AUTO-CANCEL TIMEOUT
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

                        LocalStorage.saveActiveDriverId(this@WaitingForApprovalActivity, driver.driverId)

                        startActivity(
                            Intent(this@WaitingForApprovalActivity, DriverDetailsActivity::class.java)
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
    // BOOKING DETAILS POPUP
    // =====================================================

    private fun openBookingDetailsPopup() {

        val dialog = BottomSheetDialog(this)
        val view   = layoutInflater.inflate(R.layout.bottom_sheet_booking_details, null)

        view.findViewById<TextView>(R.id.tvPickup).text = LocalStorage.getPickupAddress(this)
        view.findViewById<TextView>(R.id.tvDrop).text   = LocalStorage.getDropAddress(this)

        view.findViewById<TextView>(R.id.tvGoods).text =
            LocalStorage.getGoodsType(this).ifBlank { "—" }

        view.findViewById<TextView>(R.id.tvSender).text =
            "${LocalStorage.getSenderName(this)} (${LocalStorage.getSenderType(this)})\n" +
                    "${LocalStorage.getSenderHouse(this)}\n" +
                    LocalStorage.getSenderPhone(this)

        view.findViewById<TextView>(R.id.tvReceiver).text =
            "${LocalStorage.getReceiverName(this)} (${LocalStorage.getReceiverType(this)})\n" +
                    "${LocalStorage.getReceiverHouse(this)}\n" +
                    LocalStorage.getReceiverPhone(this)

        view.findViewById<Button>(R.id.btnClose).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    // =====================================================
    // SUPPORT POPUP
    // =====================================================

    private fun openSupportPopup() {

        val dialog = BottomSheetDialog(this)
        val view   = layoutInflater.inflate(R.layout.bottom_sheet_support, null)

        view.findViewById<Button>(R.id.btnCloseSupport).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    // =====================================================
    // MANUAL CANCEL
    // =====================================================

    private fun manualCancel() {
        pollingJob?.cancel()
        cancelRide {
            LocalStorage.saveActiveRideId(this, 0L)
            redirectHome()
        }
    }

    // =====================================================
    // AUTO CANCEL + RETRY OPTION
    // =====================================================

    private fun autoCancelWithRetry() {
        pollingJob?.cancel()
        cancelRide { showRetryDialog() }
    }

    // =====================================================
    // CANCEL API
    // =====================================================

    private fun cancelRide(onDone: () -> Unit) {

        val rideId = LocalStorage.getActiveRideId(this)
        val userId = LocalStorage.getUserId(this)
        val token  = LocalStorage.getToken(this)

        activityScope.launch(Dispatchers.IO) {
            try {
                if (rideId != -1L && userId != -1 && !token.isNullOrEmpty()) {
                    ApiClient.api.cancelRide(
                        rideId     = rideId,
                        userId     = userId.toLong(),
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
    // RETRY DIALOG
    // =====================================================

    private fun showRetryDialog() {
        AlertDialog.Builder(this)
            .setTitle("No driver found")
            .setMessage("No driver accepted your request.\nWould you like to try again?")
            .setCancelable(false)
            .setPositiveButton("Try Again") { _, _ -> retryRide() }
            .setNegativeButton("Go Back")   { _, _ -> redirectHome() }
            .show()
    }

    // =====================================================
    // RETRY LOGIC
    // =====================================================

    private fun retryRide() {
        startActivity(Intent(this, FinalFareActivity::class.java))
        finish()
    }

    // =====================================================
    // CLEAR LOCAL STATE
    // =====================================================

    private fun clearRideData() {
        LocalStorage.saveActiveRideId(this, 0L)
        LocalStorage.saveActiveDriverId(this, -1L)
    }

    // =====================================================
    // HOME
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