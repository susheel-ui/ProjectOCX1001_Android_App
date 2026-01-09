package com.example.project_a_android_userapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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

class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var txtName: TextView
    private lateinit var txtPhone: TextView
    private lateinit var btnCall: ImageButton
    private lateinit var imgVehicleType: ImageView

    private lateinit var vm: LocationViewModel

    private var driverPhone = ""
    private var driverId: Long = -1L
    private var vehicleType = "TRUCK"

    private var isCallingInProgress = false

    private val handler = Handler(Looper.getMainLooper())
    private var driverMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private var routeFetched = false

    private var pickupLat = 0.0
    private var pickupLng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_details)

        vm = (application as MyApp).vm
        pickupLat = vm.pickupLat
        pickupLng = vm.pickupLon

        if (pickupLat == 0.0 || pickupLng == 0.0) {
            toast("Pickup location missing")
            finish()
            return
        }

        txtName = findViewById(R.id.driverName)
        txtPhone = findViewById(R.id.driverPhone)
        btnCall = findViewById(R.id.btnCallDriver)
        imgVehicleType = findViewById(R.id.imgVehicleType)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val rideId = LocalStorage.getActiveRideId(this)
        if (rideId <= 0) {
            toast("Ride not found")
            finish()
            return
        }

        fetchDriverContact(rideId)

        btnCall.setOnClickListener {

            // Prevent multiple taps (Ola/Uber style)
            if (isCallingInProgress) return@setOnClickListener

            val rideId = LocalStorage.getActiveRideId(this)
            if (rideId <= 0) {
                toast("Ride not active")
                return@setOnClickListener
            }

            val token = LocalStorage.getToken(this)
            if (token.isNullOrEmpty()) {
                toast("Session expired")
                return@setOnClickListener
            }

            isCallingInProgress = true
            btnCall.isEnabled = false

            toast("Connecting call…")

            val auth = "Bearer $token"
            val body = mapOf("rideId" to rideId)

            ApiClient.api.callDriver(auth, body)
                .enqueue(object : Callback<String> {

                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {
                        isCallingInProgress = false
                        btnCall.isEnabled = true

                        if (response.isSuccessful) {
                            // OLA / UBER STYLE MESSAGE
                            toast("You will receive a call shortly")
                        } else {
                            toast("Unable to connect call")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        isCallingInProgress = false
                        btnCall.isEnabled = true
                        toast("Call connection failed")
                    }
                })
        }


    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        val pickupPos = LatLng(pickupLat, pickupLng)
        googleMap.addMarker(
            MarkerOptions()
                .position(pickupPos)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupPos, 14f))
    }

    private fun fetchDriverContact(rideId: Long) {
        ApiClient.api.getDriverContact(rideId)
            .enqueue(object : Callback<DriverContactResponse> {
                override fun onResponse(
                    call: Call<DriverContactResponse>,
                    response: Response<DriverContactResponse>
                ) {
                    if (!response.isSuccessful || response.body() == null) return

                    val driver = response.body()!!
                    txtName.text = driver.driverName
                    txtPhone.text = driver.driverPhone

                    driverPhone = driver.driverPhone
                    driverId = driver.driverId
                    vehicleType = driver.vehicleType

                    when (vehicleType) {
                        "LOADER" -> imgVehicleType.setImageResource(R.drawable.loader)
                        "BIKE" -> imgVehicleType.setImageResource(R.drawable.mini_3w)
                        else -> imgVehicleType.setImageResource(R.drawable.truck)
                    }

                    if (driverId > 0) startLiveTracking()
                }

                override fun onFailure(call: Call<DriverContactResponse>, t: Throwable) {}
            })
    }

    private fun startLiveTracking() {
        handler.post(object : Runnable {
            override fun run() {
                if (driverId > 0) fetchDriverLocation()
                handler.postDelayed(this, 5000)
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
                    if (!response.isSuccessful || response.body() == null) return

                    val loc = response.body()!!

                    if (!routeFetched) {
                        fetchRoadRouteOnce(loc.latitude, loc.longitude)
                        routeFetched = true
                    }

                    updateDriverMarker(loc.latitude, loc.longitude)
                }

                override fun onFailure(call: Call<DriverLiveLocationResponse>, t: Throwable) {}
            })
    }

    private fun updateDriverMarker(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)

        val iconRes = when (vehicleType) {
            "LOADER" -> R.drawable.loader
            "BIKE" -> R.drawable.mini_3w
            else -> R.drawable.truck
        }

        val icon = BitmapDescriptorFactory.fromResource(iconRes)

        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(
                MarkerOptions().position(pos).title("Driver").icon(icon)
            )
        } else {
            driverMarker!!.position = pos
        }
    }

    private fun fetchRoadRouteOnce(driverLat: Double, driverLng: Double) {

        val apiKey = try {
            packageManager.getApplicationInfo(packageName, 0)
                .metaData.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        } ?: return

        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$driverLat,$driverLng" +
                    "&destination=$pickupLat,$pickupLng" +
                    "&mode=driving" +
                    "&key=$apiKey"

        Thread {
            try {
                val response = OkHttpClient()
                    .newCall(Request.Builder().url(url).build())
                    .execute()

                val data = response.body?.string() ?: return@Thread
                val json = JSONObject(data)

                if (json.getString("status") != "OK") return@Thread

                val points =
                    json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                val decodedPath = PolyUtil.decode(points)

                runOnUiThread {
                    routePolyline?.remove()
                    routePolyline = googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(decodedPath)
                            .width(16f)
                            .color(0xFF1E88E5.toInt())
                    )

                    val boundsBuilder = LatLngBounds.Builder()
                    decodedPath.forEach { boundsBuilder.include(it) }
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
                    )
                }

            } catch (e: Exception) {
                Log.e("DriverDetails", "Route error", e)
            }
        }.start()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
