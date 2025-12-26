package com.example.project_a_android_userapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_a_android_userapp.api.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var txtName: TextView
    private lateinit var txtPhone: TextView
    private lateinit var btnCall: ImageButton

    private var driverPhone = ""
    private var driverId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_details)

        // ===== MAP INIT =====
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ===== VIEWS =====
        txtName = findViewById(R.id.driverName)
        txtPhone = findViewById(R.id.driverPhone)
        btnCall = findViewById(R.id.btnCallDriver)

        // ===== RIDE ID =====
        val rideId = LocalStorage.getActiveRideId(this)
        if (rideId == -1L) {
            toast("Ride not found")
            finish()
            return
        }

        fetchDriverContact(rideId)

        btnCall.setOnClickListener {
            if (driverPhone.isNotEmpty()) {
                startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$driverPhone"))
                )
            }
        }
    }

    // ================= MAP =================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    // ================= DRIVER CONTACT =================

    private fun fetchDriverContact(rideId: Long) {

        ApiClient.api.getDriverContact(rideId)
            .enqueue(object : Callback<DriverContactResponse> {

                override fun onResponse(
                    call: Call<DriverContactResponse>,
                    response: Response<DriverContactResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val driver = response.body()!!

                        txtName.text = driver.driverName
                        txtPhone.text = driver.driverPhone
                        driverPhone = driver.driverPhone

                        // 🔥 SAVE DRIVER ID
                        driverId = driver.driverId
                        LocalStorage.saveActiveDriverId(this@DriverDetailsActivity, driverId)

                        fetchDriverLocation(driverId)
                    }
                }

                override fun onFailure(call: Call<DriverContactResponse>, t: Throwable) {
                    toast("Driver info unavailable")
                }
            })
    }

    // ================= DRIVER LOCATION =================

    private fun fetchDriverLocation(driverId: Long) {

        ApiClient.api.getDriverLiveLocation(driverId)
            .enqueue(object : Callback<DriverLiveLocationResponse> {

                override fun onResponse(
                    call: Call<DriverLiveLocationResponse>,
                    response: Response<DriverLiveLocationResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val loc = response.body()!!
                        showDriverOnMap(loc.latitude, loc.longitude)
                    }
                }

                override fun onFailure(call: Call<DriverLiveLocationResponse>, t: Throwable) {
                    toast("Location unavailable")
                }
            })
    }

    private fun showDriverOnMap(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions().position(pos).title("Driver Location")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
