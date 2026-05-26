package com.zarkit.zarkit_user.Fragements

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.zarkit.zarkit_user.BulkOrderActivity
import com.zarkit.zarkit_user.DriverDetailsActivity
import com.zarkit.zarkit_user.LocalStorage
import com.zarkit.zarkit_user.Pickup_Drop_Selector_Activity
import com.zarkit.zarkit_user.R
import com.zarkit.zarkit_user.api.ApiClient
import com.zarkit.zarkit_user.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                fetchCurrentLocation()
            } else {
                _binding?.txtPickupAddress?.text = "Location permission denied"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationPermission()

        binding.pickupCard.setOnClickListener {
            openPickupDrop(autoPickup = true)
        }

        binding.bulkOrderCard.setOnClickListener {
            startActivity(Intent(requireContext(), BulkOrderActivity::class.java))
        }

        binding.btnExploreNow.setOnClickListener {
            startActivity(Intent(requireContext(), BulkOrderActivity::class.java))
        }

        binding.viewtwowheeler.setOnClickListener { openPickupDrop(true) }
        binding.viewthreewheeler.setOnClickListener { openPickupDrop(true) }
        binding.viewfourwheeler.setOnClickListener { openPickupDrop(true) }

        binding.btnChangeAddress.setOnClickListener {
            openPickupDrop(autoPickup = false)
        }

        binding.btnLiveTrip.setOnClickListener {
            startActivity(Intent(requireContext(), DriverDetailsActivity::class.java))
        }

        checkActiveRide()
        checkRideStatusFromServer()
    }

    override fun onResume() {
        super.onResume()
        if (view != null) {
            checkActiveRide()
            checkRideStatusFromServer()
        }
    }

    // ================= RIDE STATUS API CHECK =================

    private fun checkRideStatusFromServer() {
        val ctx = context ?: return
        val activeRideId = LocalStorage.getActiveRideId(ctx)
        if (activeRideId <= 0) return

        ApiClient.api.getOnlyRideStatus(activeRideId)
            .enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (!isAdded || _binding == null) return

                    if (response.isSuccessful) {
                        val status = response.body()
                            ?.replace("\"", "")
                            ?.trim()

                        when (status) {
                            "PENDING", "ACCEPTED", "STARTED" -> { /* Do nothing */ }
                            "COMPLETED" -> showRideCompletedPopup()
                            "CANCELLED" -> showRideCancelledPopup()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    // Do nothing
                }
            })
    }

    private fun showRideCompletedPopup() {
        val ctx = context ?: return

        AlertDialog.Builder(ctx)
            .setTitle("Thank You")
            .setMessage("Thank you for choosing Zarkit. Your past ride is completed successfully.")
            .setPositiveButton("OK") { dialog, _ ->
                LocalStorage.saveActiveRideId(ctx, 0)
                checkActiveRide()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showRideCancelledPopup() {
        val ctx = context ?: return

        AlertDialog.Builder(ctx)
            .setTitle("Ride Cancelled")
            .setMessage(
                "Due to unforeseen circumstances, your ride has been cancelled in accordance with our service policies.\n\n" +
                        "We sincerely apologize for the inconvenience caused and appreciate your understanding. We are committed to providing you a better experience in the future.\n\n" +
                        "Thank you,\nZarkit Team\n\n" +
                        "For assistance, please call us at:\n+91-9876543210"
            )
            .setPositiveButton("OK") { dialog, _ ->
                LocalStorage.saveActiveRideId(ctx, 0)
                checkActiveRide()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // ================= LIVE RIDE BUTTON =================

    private fun checkActiveRide() {
        val ctx = context ?: return
        val activeRideId = LocalStorage.getActiveRideId(ctx)

        if (activeRideId > 0) {
            _binding?.btnLiveTrip?.visibility = View.VISIBLE
            startLiveButtonAnimation()
        } else {
            _binding?.btnLiveTrip?.clearAnimation()
            _binding?.btnLiveTrip?.visibility = View.GONE
        }
    }

    private fun startLiveButtonAnimation() {
        val ctx = context ?: return
        val anim = AnimationUtils.loadAnimation(ctx, R.anim.live_pulse)
        _binding?.btnLiveTrip?.startAnimation(anim)
    }

    // ================= NAVIGATION =================

    private fun openPickupDrop(autoPickup: Boolean) {
        val ctx = context ?: return
        val intent = Intent(ctx, Pickup_Drop_Selector_Activity::class.java)
        intent.putExtra("AUTO_PICKUP", autoPickup)
        startActivity(intent)
    }

    // ================= LOCATION =================

    private fun checkLocationPermission() {
        val ctx = context ?: return

        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchCurrentLocation() {
        val ctx = context ?: return

        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                if (location != null) {
                    LocalStorage.savePickupLocation(ctx, location.latitude, location.longitude)
                    fetchAddressFromLatLng(location.latitude, location.longitude)
                } else {
                    _binding?.txtPickupAddress?.text = "Unable to get location"
                }
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                _binding?.txtPickupAddress?.text = "Location error"
            }
    }

    private fun fetchAddressFromLatLng(lat: Double, lng: Double) {
        val ctx = context ?: return

        Thread {
            try {
                val geocoder = Geocoder(ctx, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                val addressText = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    listOfNotNull(
                        addr.subLocality,
                        addr.locality,
                        addr.adminArea
                    ).joinToString(", ")
                } else {
                    "Current location"
                }

                // Save address to LocalStorage
                LocalStorage.savePickupLocation(ctx, lat, lng, addressText)

                if (!isAdded || _binding == null) return@Thread

                activity?.runOnUiThread {
                    if (!isAdded || _binding == null) return@runOnUiThread
                    _binding?.txtPickupAddress?.text = addressText
                }

            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@Thread

                activity?.runOnUiThread {
                    if (!isAdded || _binding == null) return@runOnUiThread
                    _binding?.txtPickupAddress?.text = "Location unavailable"
                }
            }
        }.start()
    }

    // ================= CLEANUP =================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.btnLiveTrip?.clearAnimation()
        _binding = null
    }
}