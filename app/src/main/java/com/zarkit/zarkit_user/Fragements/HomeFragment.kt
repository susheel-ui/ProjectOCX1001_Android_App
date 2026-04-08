package com.zarkit.zarkit_user.Fragements

import android.Manifest
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
import com.zarkit.zarkit_user.MyApp
import com.zarkit.zarkit_user.LocationViewModel
import com.zarkit.zarkit_user.Pickup_Drop_Selector_Activity
import com.zarkit.zarkit_user.LocalStorage
import com.zarkit.zarkit_user.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
import com.zarkit.zarkit_user.R

class HomeFragment : Fragment() {

    // ✅ FIX 1: Nullable binding — prevents memory leaks and crashes after onDestroyView
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var vm: LocationViewModel

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                fetchCurrentLocation()
            } else {
                // ✅ FIX 2: Safe binding access via _binding?
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

        vm = (requireActivity().application as MyApp).vm

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

        checkActiveRide()

        binding.btnLiveTrip.setOnClickListener {
            startActivity(Intent(requireContext(), DriverDetailsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // ✅ FIX 3: Guard onResume — fragment view might not exist yet in edge cases
        if (view != null) {
            checkActiveRide()
        }
    }


    // ================= LIVE RIDE BUTTON =================

    private fun checkActiveRide() {
        // ✅ FIX 4: Use context safely
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
                // ✅ FIX 5: Guard after async callback
                if (!isAdded || _binding == null) return@addOnSuccessListener

                if (location != null) {
                    vm.pickupLat = location.latitude
                    vm.pickupLon = location.longitude
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
        // ✅ FIX 6: Capture context before entering thread — this is the ROOT CAUSE of your crash
        // Never call requireContext() or requireActivity() inside a Thread
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

                vm.pickupAddress = addressText

                // ✅ FIX 7: Guard before touching UI after background thread finishes
                // Fragment may have detached while geocoder was running
                if (!isAdded || _binding == null) return@Thread

                activity?.runOnUiThread {
                    // ✅ FIX 8: Double-check inside runOnUiThread too
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
        // ✅ FIX 9: Nullify binding to prevent memory leaks
        _binding?.btnLiveTrip?.clearAnimation()
        _binding = null
    }
}