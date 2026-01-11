package com.example.project_a_android_userapp.Fragements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project_a_android_userapp.Pickup_Drop_Selector_Activity
import com.example.project_a_android_userapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupClicks()

        return binding.root
    }

    private fun setupClicks() {

        // Pickup card click
        binding.pickupCard.setOnClickListener {
            openPickupScreen(null)
        }

        // 3 Wheeler
        binding.cardThreeWheeler.setOnClickListener {
            openPickupScreen("THREE_WHEELER")
        }

        // 2 Wheeler
        binding.cardTwoWheeler.setOnClickListener {
            openPickupScreen("TWO_WHEELER")
        }

        // Truck
        binding.cardTruck.setOnClickListener {
            openPickupScreen("TRUCK")
        }

    }

    private fun openPickupScreen(vehicleType: String?) {
        val intent = Intent(requireContext(), Pickup_Drop_Selector_Activity::class.java)

        // pass vehicle type only if selected
        vehicleType?.let {
            intent.putExtra("VEHICLE_TYPE", it)
        }

        startActivity(intent)
    }
}
