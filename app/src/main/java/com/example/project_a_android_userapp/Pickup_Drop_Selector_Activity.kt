package com.example.project_a_android_userapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class Pickup_Drop_Selector_Activity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var pickupEdit: EditText
    private lateinit var dropEdit: EditText
    private lateinit var submitButton: Button

    private var pickupMarker: Marker? = null
    private var dropMarker: Marker? = null
    private var pickupPoint: GeoPoint? = null
    private var dropPoint: GeoPoint? = null

    private val client = OkHttpClient()
    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_pickup_drop_selector)

        mapView = findViewById(R.id.mapView)
        pickupEdit = findViewById(R.id.pickupEdit)
        dropEdit = findViewById(R.id.dropEdit)
        submitButton = findViewById(R.id.submitButton)

        // Initialize 2D Map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(25.4489, 78.5683)) // Jhansi

        // Tap to select pickup/drop
        mapView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val geo = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt())
                val point = GeoPoint(geo.latitude, geo.longitude)

                if (pickupPoint == null) setPickup(point)
                else setDrop(point)
            }
            true
        }

        // Autocomplete for pickup/drop
        pickupEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) fetchAutocomplete(pickupEdit.text.toString()) { point -> point?.let { setPickup(it) } }
        }
        dropEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) fetchAutocomplete(dropEdit.text.toString()) { point -> point?.let { setDrop(it) } }
        }

        submitButton.setOnClickListener {
            if (pickupPoint != null && dropPoint != null) {
                val intent = android.content.Intent(this, SenderDetailsActivity::class.java)
                intent.putExtra("pickupLat", pickupPoint!!.latitude)
                intent.putExtra("pickupLon", pickupPoint!!.longitude)
                intent.putExtra("dropLat", dropPoint!!.latitude)
                intent.putExtra("dropLon", dropPoint!!.longitude)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select both pickup and drop points", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setPickup(point: GeoPoint) {
        pickupMarker?.let { mapView.overlays.remove(it) }
        pickupMarker = Marker(mapView).apply {
            position = point
            title = "Pickup"
            mapView.overlays.add(this)
        }
        pickupPoint = point
        mapView.controller.animateTo(point)
        mapView.invalidate()
        Toast.makeText(this, "Pickup selected", Toast.LENGTH_SHORT).show()
    }

    private fun setDrop(point: GeoPoint) {
        dropMarker?.let { mapView.overlays.remove(it) }
        dropMarker = Marker(mapView).apply {
            position = point
            title = "Drop"
            mapView.overlays.add(this)
        }
        dropPoint = point
        mapView.controller.animateTo(point)
        mapView.invalidate()
        Toast.makeText(this, "Drop selected", Toast.LENGTH_SHORT).show()
    }

    private fun fetchAutocomplete(query: String, callback: (GeoPoint?) -> Unit) {
        if (query.isEmpty()) return
        val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=1"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(url).header("User-Agent", "OSMApp").build()
                val response = client.newCall(request).execute()
                val array = JSONArray(response.body?.string())
                if (array.length() > 0) {
                    val obj = array.getJSONObject(0)
                    val lat = obj.getDouble("lat")
                    val lon = obj.getDouble("lon")
                    withContext(Dispatchers.Main) { callback(GeoPoint(lat, lon)) }
                } else withContext(Dispatchers.Main) { callback(null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }
}
