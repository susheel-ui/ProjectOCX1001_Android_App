package com.example.project_a_android_userapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import androidx.core.content.ContextCompat
import com.example.project_a_android_userapp.Fragements.HomeFragment
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
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import okhttp3.ResponseBody

class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback,PaymentResultWithDataListener  {

    companion object {
        private const val DIRECTIONS_API_KEY = "AIzaSyAk5HjRT_tihvIZ7Y0ZQbcvpzn0yOSM8ac"
    }

    // MAP
    private lateinit var googleMap: GoogleMap
    private var mapReady = false

    // UI
    private lateinit var txtName: TextView
    private lateinit var txtStatus: TextView
    private lateinit var btnCall: ImageButton
    private lateinit var imgVehicleType: ImageView
    private lateinit var btnCancelRide: Button
    private lateinit var btnPay: Button
    private var isRideStarted = false

    private var destinationMarker: Marker? = null

    private var startedHandled = false

    private lateinit var txtRideId: TextView
    private lateinit var txtVehicleNumber: TextView

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

    private var lastMarkerLatLng: LatLng? = null

    private lateinit var btnBack: ImageButton
    private val ROUTE_UPDATE_DISTANCE_METERS = 50f

    private lateinit var txtFare: TextView

    private var razorpayOrderId: String = ""
    private var razorpayPaymentId: String = ""
    private var razorpaySignature: String = ""

    private var isPaymentDone = false

    private lateinit var txtPaymentStatus: TextView



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

        // 🔥 FALLBACK: load from LocalStorage if VM is empty
        if (pickupLat == 0.0 || pickupLng == 0.0) {
            pickupLat = LocalStorage.getPickupLat(this)
            pickupLng = LocalStorage.getPickupLng(this)
        }

        if (dropLat == 0.0 || dropLon == 0.0) {
            dropLat = LocalStorage.getDropLat(this)
            dropLon = LocalStorage.getDropLng(this)
        }

        rideId = LocalStorage.getActiveRideId(this)

        // 🔥 ONLY check pickup if NOT coming from active ride
        if (rideId <= 0 && (pickupLat == 0.0 || pickupLng == 0.0)) {
            toast("Pickup location missing")
            finish()
            return
        }

        txtName = findViewById(R.id.driverName)
        txtStatus = findViewById(R.id.tvRideStatus)
        btnCall = findViewById(R.id.btnCallDriver)
        imgVehicleType = findViewById(R.id.imgVehicleType)
        btnCancelRide = findViewById(R.id.btnCancelRide)
        btnPay = findViewById(R.id.btnPay)

        btnBack = findViewById(R.id.btnBack)
        txtRideId = findViewById(R.id.tvRideId)
        txtVehicleNumber = findViewById(R.id.tvVehicleNumber)
        txtFare = findViewById(R.id.tvFare)
        txtPaymentStatus = findViewById(R.id.tvPaymentStatus)

        btnBack.setOnClickListener {
            val intent = Intent(this, Home_Activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }


        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        rideId = LocalStorage.getActiveRideId(this)
        txtRideId.text = "Ride ID: Zarkit_OCX_$rideId"
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

    private fun bitmapFromDrawable(
        context: Context,
        drawableId: Int,
        width: Int = 120,
        height: Int = 120
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    // ================= DRIVER =================

    private fun refreshDriverMarkerIcon() {
        driverMarker?.let { marker ->
            val newIcon = bitmapFromDrawable(
                this,
                getVehicleIconByType(vehicleType),
                110, //  bigger looks better
                110
            )
            marker.setIcon(newIcon)
        }
    }

    private fun fetchDriverContact(rideId: Long) {

        ApiClient.api.getDriverContact(rideId)
            .enqueue(object : Callback<DriverContactResponse> {

                override fun onResponse(
                    call: Call<DriverContactResponse>,
                    response: Response<DriverContactResponse>
                ) {

                    val driver = response.body() ?: return

                    txtName.text = driver.driverName
                    txtStatus.text = "Status: ${driver.rideStatus}"
                    txtVehicleNumber.text = "Vehicle No: ${driver.vehicleNumber}"
                    txtFare.text = "Fare: ₹ ${driver.finalFare}"

                    driverId = driver.driverId
                    vehicleType = driver.vehicleType

                    refreshDriverMarkerIcon()
                    handleRideStatus(driver.rideStatus ?: "PENDING")

                    // ✅ DRIVER IMAGE LOAD
                    driver.driverPhotoUrl?.let {
                        loadDriverImage(it)
                    }

                    isPaymentDone = driver.isPaymentDone ?: false
                    if (isPaymentDone) {
                        btnPay.visibility = View.GONE
                        txtPaymentStatus.visibility = View.VISIBLE // ✅ show payment done
                    } else {
                        btnPay.visibility = View.VISIBLE
                        txtPaymentStatus.visibility = View.GONE
                    }

                    startLiveTracking()
                }

                override fun onFailure(call: Call<DriverContactResponse>, t: Throwable) {}

            })
    }

    private fun loadDriverImage(fileName: String) {

        ApiClient.api.getDriverImage(fileName)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    if (!response.isSuccessful) return

                    val stream = response.body()?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(stream)

                    runOnUiThread {
                        imgVehicleType.setImageBitmap(bitmap)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
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
                btnPay.visibility = if (isPaymentDone) View.GONE else View.VISIBLE
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
            createOrderApiAndStartPayment()
        }
    }

    private fun createOrderApiAndStartPayment() {

        val token = "Bearer ${LocalStorage.getToken(this)}"

        ApiClient.api.createOrder(rideId, token)
            .enqueue(object : Callback<CreateOrderResponse> {

                override fun onResponse(
                    call: Call<CreateOrderResponse>,
                    response: Response<CreateOrderResponse>
                ) {

                    val body = response.body() ?: return

                    razorpayOrderId = body.orderId

                    startRazorpayPayment(
                        orderId = body.orderId,
                        amount = body.amount
                    )
                }

                override fun onFailure(call: Call<CreateOrderResponse>, t: Throwable) {
                    toast("Order creation failed")
                }
            })
    }

    // ================= VERIFY PAYMENT API =================

    private fun verifyPaymentApi() {
        val token = "Bearer ${LocalStorage.getToken(this)}"

        val request = VerifyPaymentRequest(
            rideId = rideId,
            paymentId = razorpayPaymentId,
            orderId = razorpayOrderId,
            signature = razorpaySignature
        )

        ApiClient.api.verifyPayment(token, request)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        toast("Payment Verified ✅")

                        runOnUiThread {
                            btnPay.visibility = View.GONE
                            txtPaymentStatus.visibility = View.VISIBLE
                        }
                        fetchDriverContact(rideId);
                    } else {
                        toast("Verification Failed")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    toast("Verification API Failed")
                }
            })
    }

    private fun startRazorpayPayment(orderId: String, amount: Int) {

        val checkout = Checkout()
        checkout.setKeyID("rzp_test_SFEc0SwSaQSBkk")

        try {

            val options = JSONObject()
            options.put("name", "ZARKIT")
            options.put("description", "Ride Fare Payment")
            options.put("currency", "INR")
            options.put("amount", amount)
            options.put("order_id", orderId)

            val prefill = JSONObject()
            prefill.put("contact", vm.senderPhone)
            prefill.put("email", "test@email.com")

            options.put("prefill", prefill)

            checkout.open(this, options)

        } catch (e: Exception) {
            Log.e("RAZORPAY_ERROR", "Payment start error", e)
            Toast.makeText(this, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ================= PAYMENT SUCCESS =================

    override fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
        if (paymentId == null || paymentData == null) {
            Toast.makeText(this, "Payment data missing", Toast.LENGTH_LONG).show()
            return
        }

        // Save Razorpay details
        razorpayPaymentId = paymentId
        razorpayOrderId = paymentData.orderId ?: ""
        razorpaySignature = paymentData.signature ?: ""

        Log.d("RAZORPAY_DEBUG", "PaymentId: $razorpayPaymentId")
        Log.d("RAZORPAY_DEBUG", "OrderId: $razorpayOrderId")
        Log.d("RAZORPAY_DEBUG", "Signature: $razorpaySignature")

        // Call your verify API
        verifyPaymentApi()
    }


    override fun onPaymentError(
        code: Int,
        response: String?,
        paymentData: PaymentData?
    ) {
        Toast.makeText(
            this,
            "Payment Failed",
            Toast.LENGTH_LONG
        ).show()
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
                        LocalStorage.saveActiveRideId(this@DriverDetailsActivity, 0L)
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

    private fun getVehicleIconByType(vehicleType: String): Int {
        return when {
            vehicleType.contains("TWO", true) ||
                    vehicleType.contains("BIKE", true) ->
                R.drawable.v2wlive

            vehicleType.contains("THREE", true) ||
                    vehicleType.contains("LOADER", true) ->
                R.drawable.v3wlive

            vehicleType.contains("FOUR", true) ||
                    vehicleType.contains("TRUCK", true) ->
                R.drawable.v4wlive

            else ->
                R.drawable.v2wlive
        }
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
                LocalStorage.clearActiveRide(this)
                LocalStorage.saveActiveRideId(this, 0L)
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
                if (!isFinishing && !isDestroyed) {
                    handler.postDelayed(this, 5000)
                }

            }
        })
    }

    private fun pollRideStatus() {

        ApiClient.api.getRideStatus(rideId)
            .enqueue(object : Callback<RideStatusResponse> {

                override fun onResponse(
                    call: Call<RideStatusResponse>,
                    response: Response<RideStatusResponse>
                ) {

                    val status = response.body()?.rideStatus ?: return

                    handleRideStatus(status)
                }

                override fun onFailure(call: Call<RideStatusResponse>, t: Throwable) {
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

                    if (lastRouteLatLng == null || shouldUpdateRoute(pos)) {
                        fetchRoadRoute(pos)
                        lastRouteLatLng = pos
                    }
                }

                override fun onFailure(call: Call<DriverLiveLocationResponse>, t: Throwable) {}
            })
    }

    private fun getBearing(from: LatLng, to: LatLng): Float {
        val start = Location("start").apply {
            latitude = from.latitude
            longitude = from.longitude
        }
        val end = Location("end").apply {
            latitude = to.latitude
            longitude = to.longitude
        }
        return start.bearingTo(end)
    }


    private fun updateDriverMarker(pos: LatLng) {

        if (driverMarker == null) {

            val bikeIcon = bitmapFromDrawable(
                this,
                getVehicleIconByType(vehicleType),
                120,
                140
            )

            driverMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("Driver")
                    .icon(bikeIcon)
                    .anchor(0.5f, 0.5f)
                    .flat(true) // REQUIRED for rotation
            )

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
            lastMarkerLatLng = pos
            return
        }

        // 🔄 ROTATE towards movement direction
        lastMarkerLatLng?.let { lastPos ->
            val bearing = getBearing(lastPos, pos)
            driverMarker?.rotation = bearing
        }

        driverMarker?.position = pos
        lastMarkerLatLng = pos
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

//    private fun startTestPayment() {
//
//        if (vm.finalFare <= 0) {
//            toast("Invalid fare")
//            return
//        }
//
//        val amountInPaise = (vm.finalFare * 100).toInt()
//
//        val checkout = Checkout()
//
//        // 🔑 Razorpay TEST KEY ONLY
//        checkout.setKeyID("rzp_test_S5hfApqAqe4arW") // <-- put your test key here
//
//        val options = JSONObject()
//        options.put("name", "ZARKIT")
//        options.put("description", "Ride Fare Payment")
//        options.put("currency", "INR")
//        options.put("amount", amountInPaise)
//
//        options.put("theme.color", "#1E88E5")
//
//        val prefill = JSONObject()
//        prefill.put("contact", vm.senderPhone)
//        prefill.put("email", "testuser@email.com")
//
//        options.put("prefill", prefill)
//
//        try {
//            checkout.open(this, options)
//        } catch (e: Exception) {
//            toast("Unable to start payment")
//        }
//    }
//



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
