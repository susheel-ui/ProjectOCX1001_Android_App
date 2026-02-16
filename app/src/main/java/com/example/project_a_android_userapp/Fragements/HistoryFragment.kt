package com.example.project_a_android_userapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_a_android_userapp.FareActivity
import com.example.project_a_android_userapp.LocationViewModel
import com.example.project_a_android_userapp.MyApp
import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.adapter.BookingAdapter
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.LocalStorage
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var illustrationImage: ImageView
    private lateinit var textNoHistory: TextView
    private lateinit var textSubMessage: TextView

    private lateinit var bookingAdapter: BookingAdapter

    // 🔥 Shared ViewModel
    private lateinit var vm: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize Views
        rvBookings = view.findViewById(R.id.rvBookings)
        illustrationImage = view.findViewById(R.id.illustration_image)
        textNoHistory = view.findViewById(R.id.text_no_history)
        textSubMessage = view.findViewById(R.id.text_sub_message)

        rvBookings.layoutManager = LinearLayoutManager(requireContext())

        // Init ViewModel
        vm = (requireActivity().application as MyApp).vm

        // Load history directly
        fetchBookingHistory()

        return view
    }

    private fun fetchBookingHistory() {

        lifecycleScope.launch {

            try {

                val userId = LocalStorage.getUserId(requireContext())
                val response = ApiClient.api.getAllBookings(userId)

                if (response.isSuccessful && response.body() != null) {

                    val bookingList = response.body()!!

                    if (bookingList.isNotEmpty()) {

                        rvBookings.visibility = View.VISIBLE
                        illustrationImage.visibility = View.GONE
                        textNoHistory.visibility = View.GONE
                        textSubMessage.visibility = View.GONE

                        bookingAdapter = BookingAdapter(
                            bookingList,
                            onRebookClick = { rideId ->
                                rebookRide(rideId)
                            },
                            onInvoiceClick = {
                                // keep empty for now
                            }
                        )

                        rvBookings.adapter = bookingAdapter

                    } else {
                        showEmptyState()
                    }

                } else {
                    Log.e(
                        "HistoryFragment",
                        "API Error: ${response.code()} - ${response.message()}"
                    )
                    showEmptyState()
                }

            } catch (e: Exception) {
                Log.e("HistoryFragment", "Exception: ${e.localizedMessage}")
                showEmptyState()
            }
        }
    }

    // 🔥 REBOOK FLOW (UNCHANGED)
    private fun rebookRide(rideId: Long) {

        lifecycleScope.launch {

            try {
                val token = LocalStorage.getToken(requireContext()) ?: return@launch

                val response =
                    ApiClient.api.bookAgain("Bearer $token", rideId)

                if (response.isSuccessful && response.body() != null) {

                    vm.setFromRebookResponse(response.body()!!)

                    startActivity(
                        Intent(requireContext(), FareActivity::class.java)
                    )

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Unable to rebook",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("REBOOK", "Failed", e)
            }
        }
    }

    private fun showEmptyState() {

        rvBookings.visibility = View.GONE
        illustrationImage.visibility = View.VISIBLE
        textNoHistory.visibility = View.VISIBLE
        textSubMessage.visibility = View.VISIBLE
    }
}
