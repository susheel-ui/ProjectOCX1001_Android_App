package com.example.project_a_android_userapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class DriverDetailsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var driverNameText: TextView
    private lateinit var driverPhoneText: TextView
    private lateinit var vehicleText: TextView

    private lateinit var locationManager: LocationManager

    private var driverLocation: GeoPoint = GeoPoint(25.4495, 78.5690)
    private var userLocation: GeoPoint? = null

    private lateinit var driverMarker: Marker
    private lateinit var userMarker: Marker

    private val locationPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_driver_details)

        mapView = findViewById(R.id.mapView)
        driverNameText = findViewById(R.id.driverNameText)
        driverPhoneText = findViewById(R.id.driverPhoneText)
        vehicleText = findViewById(R.id.vehicleText)

        val vehicle = intent.getStringExtra("vehicle") ?: "Bike"

        vehicleText.text = "Vehicle: $vehicle"
        driverNameText.text = "Driver: Ravi Kumar"
        driverPhoneText.text = "Phone: +91-9876543210"

        // Initialize map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)

        driverMarker = Marker(mapView)
        driverMarker.position = driverLocation
        driverMarker.title = "Driver"
        mapView.overlays.add(driverMarker)

        userMarker = Marker(mapView)
        userMarker.title = "You"

        mapView.controller.setCenter(driverLocation)
        mapView.invalidate()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocationPermissionAndStart()
    }

    private fun checkLocationPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            startTrackingUserLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingUserLocation()
            }
        }
    }

    private fun startTrackingUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,
            1f,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                    updateUserMarker()
                    simulateDriverMovement()
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
        )
    }

    private fun updateUserMarker() {
        userLocation?.let {
            userMarker.position = it
            if (!mapView.overlays.contains(userMarker)) {
                mapView.overlays.add(userMarker)
            }
            mapView.invalidate()
        }
    }

    private var driverJob: Job? = null

    private fun simulateDriverMovement() {
        driverJob?.cancel()
        driverJob = CoroutineScope(Dispatchers.Main).launch {
            userLocation?.let { target ->
                val steps = 50
                val delayMs = 500L
                val deltaLat = (target.latitude - driverLocation.latitude) / steps
                val deltaLon = (target.longitude - driverLocation.longitude) / steps

                repeat(steps) {
                    driverLocation = GeoPoint(driverLocation.latitude + deltaLat, driverLocation.longitude + deltaLon)
                    driverMarker.position = driverLocation
                    mapView.controller.setCenter(driverLocation)
                    mapView.invalidate()
                    delay(delayMs)
                }
            }
        }
    }
}
