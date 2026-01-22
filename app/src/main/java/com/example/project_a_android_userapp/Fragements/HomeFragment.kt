package com.example.project_a_android_userapp.Fragements

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.project_a_android_userapp.MyApp
import com.example.project_a_android_userapp.LocationViewModel
import com.example.project_a_android_userapp.Pickup_Drop_Selector_Activity
import com.example.project_a_android_userapp.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ✅ USE GLOBAL INSTANCE FROM MyApp
    private lateinit var vm: LocationViewModel

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) fetchCurrentLocation()
            else binding.txtPickupAddress.text = "Location permission denied"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 THIS IS THE MOST IMPORTANT LINE
        vm = (requireActivity().application as MyApp).vm

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationPermission()

        // ✅ EXISTING PICKUP CARD (UNCHANGED)
        binding.pickupCard.setOnClickListener {
            openPickupDrop()
        }

        // ✅ ADDED: ALL VEHICLE VIEWS REDIRECT TO PICKUP/DROP
        binding.viewtwowheeler.setOnClickListener {
            openPickupDrop()
        }

        binding.viewthreewheeler.setOnClickListener {
            openPickupDrop()
        }

        binding.viewfourwheeler.setOnClickListener {
            openPickupDrop()
        }
    }

    private fun openPickupDrop() {
        startActivity(
            Intent(requireContext(), Pickup_Drop_Selector_Activity::class.java)
        )
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
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
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {

                    // ✅ SAVE IN CLASS INSTANCE
                    vm.pickupLat = location.latitude
                    vm.pickupLon = location.longitude

                    fetchAddressFromLatLng(
                        location.latitude,
                        location.longitude
                    )
                } else {
                    binding.txtPickupAddress.text = "Unable to get location"
                }
            }
            .addOnFailureListener {
                binding.txtPickupAddress.text = "Location error"
            }
    }

    private fun fetchAddressFromLatLng(lat: Double, lng: Double) {
        Thread {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
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

                // ✅ SAVE PICKUP ADDRESS
                vm.pickupAddress = addressText

                requireActivity().runOnUiThread {
                    binding.txtPickupAddress.text = addressText
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    binding.txtPickupAddress.text = "Location unavailable"
                }
            }
        }.start()
    }
}
