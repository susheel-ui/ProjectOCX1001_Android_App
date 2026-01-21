package com.example.project_a_android_userapp

import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener

class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback,PaymentResultListener  {

    companion object {
        private const val DIRECTIONS_API_KEY = "AIzaSyCoOrZcf81cqlulisAEsZhjLF192yahFkc"
    }

    // MAP
    private lateinit var googleMap: GoogleMap
    private var mapReady = false

    // UI
    private lateinit var txtName: TextView
    private lateinit var txtPhone: TextView
    private lateinit var btnCall: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var imgVehicleType: ImageView
    private lateinit var btnCancelRide: Button
    private lateinit var btnPay: Button
    private var isRideStarted = false

    private var destinationMarker: Marker? = null

    private var startedHandled = false



    // DATA
    private lateinit var vm: LocationViewModel
    private var driverId: Long = -1L
    private var vehicleType = "TRUCK"
    private var pickupLat = 0.0
    private var pickupLng = 0.0

    var dropLat: Double = 0.0

    var dropLon: Double = 0.0

    private fun getDestinationLatLng(): LatLng {
        return if (isRideStarted && dropLat != 0.0 && dropLon != 0.0) {
            LatLng(dropLat, dropLon)
        } else {
            LatLng(pickupLat, pickupLng)
        }
    }


    private var rideId: Long = -1L

    // TRACKING
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private var driverMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private var lastRouteLatLng: LatLng? = null

    private val ROUTE_UPDATE_DISTANCE_METERS = 50f

    // ================= LIFECYCLE =================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_details)

        Checkout.preload(applicationContext)

        vm = (application as MyApp).vm
        pickupLat = vm.pickupLat
        pickupLng = vm.pickupLon
        dropLat = vm.dropLat
        dropLon = vm.dropLon

        if (pickupLat == 0.0 || pickupLng == 0.0) {
            toast("Pickup location missing")
            finish()
            return
        }

        txtName = findViewById(R.id.driverName)
        txtPhone = findViewById(R.id.driverPhone)
        btnCall = findViewById(R.id.btnCallDriver)
        btnRefresh = findViewById(R.id.btnRefreshRoute)
        imgVehicleType = findViewById(R.id.imgVehicleType)
        btnCancelRide = findViewById(R.id.btnCancelRide)
        btnPay = findViewById(R.id.btnPay)

        btnRefresh.setOnClickListener { forceRefreshRoute() }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        rideId = LocalStorage.getActiveRideId(this)
        if (rideId <= 0) {
            toast("Ride not found")
            finish()
            return
        }

        fetchDriverContact(rideId)
    }

    // ================= MAP =================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapReady = true

        googleMap.uiSettings.isZoomControlsEnabled = true

        val destinationLatLng =
            if (isRideStarted && dropLat != 0.0 && dropLon != 0.0)
                LatLng(dropLat, dropLon)
            else
                LatLng(pickupLat, pickupLng)

        destinationMarker?.remove()
        destinationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(destinationLatLng)
                .title(if (isRideStarted) "Drop Location" else "Pickup Location")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        if (isRideStarted)
                            BitmapDescriptorFactory.HUE_RED
                        else
                            BitmapDescriptorFactory.HUE_GREEN
                    )
                )
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 14f))
    }

    // ================= DRIVER =================

    private fun fetchDriverContact(rideId: Long) {
        ApiClient.api.getDriverContact(rideId)
            .enqueue(object : Callback<DriverContactResponse> {
                override fun onResponse(
                    call: Call<DriverContactResponse>,
                    response: Response<DriverContactResponse>
                ) {
                    val driver = response.body() ?: return

                    txtName.text = driver.driverName
                    txtPhone.text = driver.driverPhone
                    driverId = driver.driverId
                    vehicleType = driver.vehicleType

                    handleRideStatus(driver.rideStatus ?: "PENDING")

                    imgVehicleType.setImageResource(
                        when (vehicleType) {
                            "LOADER" -> R.drawable.loader
                            "BIKE" -> R.drawable.mini_3w
                            else -> R.drawable.truck
                        }
                    )

                    startLiveTracking()
                }

                override fun onFailure(call: Call<DriverContactResponse>, t: Throwable) {}
            })
    }

    // ================= RIDE STATUS =================

    private fun handleRideStatus(status: String) {

        btnCancelRide.visibility = View.GONE
        btnPay.visibility = View.GONE

        when (status) {
            "PENDING", "ACCEPTED" -> {
                isRideStarted = false
                startedHandled = false
                btnCancelRide.visibility = View.VISIBLE
            }

            "STARTED" -> {
                btnPay.visibility = View.VISIBLE
                btnCancelRide.visibility = View.GONE

                if (!startedHandled) {
                    isRideStarted = true
                    startedHandled = true

                    lastRouteLatLng = null
                    routePolyline?.remove()
                    routePolyline = null

                    updateDestinationMarker()
                }
            }


            "COMPLETED" -> {
                startedHandled = false
                showRideCompletedPopup()
            }

            "CANCELLED" -> {
                startedHandled = false
                redirectToHome()
            }
        }

        btnCancelRide.setOnClickListener {
            showCancelConfirmationPopup()
        }

        btnPay.setOnClickListener {
            startTestPayment()
        }
    }

    private fun updateDestinationMarker() {
        if (!mapReady) return

        val destination = getDestinationLatLng()

        destinationMarker?.remove()
        destinationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(destination)
                .title(if (isRideStarted) "Drop Location" else "Pickup Location")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        if (isRideStarted)
                            BitmapDescriptorFactory.HUE_RED
                        else
                            BitmapDescriptorFactory.HUE_GREEN
                    )
                )
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 14f))
    }



    // ================= CANCEL CONFIRMATION =================

    private fun showCancelConfirmationPopup() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Ride")
            .setMessage("Are you sure you want to cancel this ride?")
            .setCancelable(false)
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelRideApi()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun cancelRideApi() {

        val userId = LocalStorage.getUserId(this).toLong()
        val rawToken = LocalStorage.getToken(this)

        if (rawToken.isNullOrEmpty()) {
            toast("Session expired. Please login again.")
            return
        }

        val token = "Bearer $rawToken"

        ApiClient.api.cancelRide(rideId, userId, token)
            .enqueue(object : Callback<Void> {

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        toast("Trip Cancelled Successfully")
                        redirectToHome()
                    } else {
                        toast("Trip started cannot be cancelled")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    toast("Network error. Please try again.")
                }
            })
    }

    // ================= REDIRECT HOME =================

    private fun redirectToHome() {
        handler.removeCallbacksAndMessages(null)
        executor.shutdown()

        val intent = Intent(this, Home_Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    // ================= COMPLETED POPUP =================

    private fun showRideCompletedPopup() {
        handler.removeCallbacksAndMessages(null)

        AlertDialog.Builder(this)
            .setTitle("Ride Completed")
            .setMessage("Your ride has been completed successfully.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                redirectToHome()
            }
            .show()
    }

    // ================= LIVE TRACK =================

    private fun startLiveTracking() {
        handler.post(object : Runnable {
            override fun run() {
                if (driverId > 0 && mapReady){
                    fetchDriverLocation()
                    pollRideStatus()
                }
                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun pollRideStatus() {
        ApiClient.api.getDriverContact(rideId)
            .enqueue(object : Callback<DriverContactResponse> {
                override fun onResponse(
                    call: Call<DriverContactResponse>,
                    response: Response<DriverContactResponse>
                ) {
                    val data = response.body() ?: return

                    // ✅ Update buttons / popup automatically
                    handleRideStatus(data.rideStatus ?: "PENDING")
                }

                override fun onFailure(call: Call<DriverContactResponse>, t: Throwable) {
                    // ignore silently
                }
            })
    }


    private fun fetchDriverLocation() {
        ApiClient.api.getDriverLiveLocation(driverId)
            .enqueue(object : Callback<DriverLiveLocationResponse> {
                override fun onResponse(
                    call: Call<DriverLiveLocationResponse>,
                    response: Response<DriverLiveLocationResponse>
                ) {
                    val loc = response.body() ?: return
                    val pos = LatLng(loc.latitude, loc.longitude)

                    updateDriverMarker(pos)

                    if (lastRouteLatLng == null) {
                        fetchRoadRoute(pos)
                        lastRouteLatLng = pos
                    } else if (shouldUpdateRoute(pos)) {
                        fetchRoadRoute(pos)
                        lastRouteLatLng = pos
                    }

                    if (shouldUpdateRoute(pos)) {
                        fetchRoadRoute(pos)
                        lastRouteLatLng = pos
                    }
                }

                override fun onFailure(call: Call<DriverLiveLocationResponse>, t: Throwable) {}
            })
    }

    private fun updateDriverMarker(pos: LatLng) {
        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(
                MarkerOptions().position(pos).title("Driver")
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
        } else {
            driverMarker!!.position = pos
        }
    }

    // ================= ROUTE =================

    private fun shouldUpdateRoute(newPos: LatLng): Boolean {
        val last = lastRouteLatLng ?: return true
        val result = FloatArray(1)
        Location.distanceBetween(
            last.latitude, last.longitude,
            newPos.latitude, newPos.longitude,
            result
        )
        return result[0] > ROUTE_UPDATE_DISTANCE_METERS
    }


    private fun fetchRoadRoute(driverPos: LatLng) {
        val destination = getDestinationLatLng()
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${driverPos.latitude},${driverPos.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=driving" +
                    "&key=$DIRECTIONS_API_KEY"


        executor.execute {
            try {
                val response = OkHttpClient()
                    .newCall(Request.Builder().url(url).build())
                    .execute()

                val body = response.body?.string() ?: return@execute
                val json = JSONObject(body)

                if (json.getString("status") != "OK") return@execute

                val polyline =
                    json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                val path = PolyUtil.decode(polyline)

                runOnUiThread {
                    routePolyline?.remove()
                    routePolyline = googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(path)
                            .width(14f)
                            .color(0xFF1E88E5.toInt())
                    )
                }

            } catch (e: Exception) {
                Log.e("ROUTE_EXCEPTION", "Route failed", e)
            }
        }
    }

    // ================= REFRESH =================

    private fun forceRefreshRoute() {
        lastRouteLatLng = null
        routePolyline?.remove()
        routePolyline = null
        toast("Refreshing route…")
        fetchDriverLocation()
    }

    private fun startTestPayment() {

        if (vm.finalFare <= 0) {
            toast("Invalid fare")
            return
        }

        val amountInPaise = (vm.finalFare * 100).toInt()

        val checkout = Checkout()

        // 🔑 Razorpay TEST KEY ONLY
        checkout.setKeyID("rzp_test_S5hfApqAqe4arW") // <-- put your test key here

        val options = JSONObject()
        options.put("name", "ZARKIT")
        options.put("description", "Ride Fare Payment")
        options.put("currency", "INR")
        options.put("amount", amountInPaise)

        options.put("theme.color", "#1E88E5")

        val prefill = JSONObject()
        prefill.put("contact", vm.senderPhone)
        prefill.put("email", "testuser@email.com")

        options.put("prefill", prefill)

        try {
            checkout.open(this, options)
        } catch (e: Exception) {
            toast("Unable to start payment")
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        toast("Payment Successful ✅")
        Log.d("RAZORPAY", "Payment ID: $paymentId")

        // For now only testing
    }

    override fun onPaymentError(code: Int, response: String?) {
        toast("Payment Failed ❌")
        Log.e("RAZORPAY", "Error $code : $response")
    }



    // ================= CLEANUP =================

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        executor.shutdown()
        super.onDestroy()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
