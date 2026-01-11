package com.example.project_a_android_userapp.Fragements

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_a_android_userapp.LocalStorage
import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.Trip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val tripList = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.rvTrips)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TripAdapter()

        loadTrips()

        return view
    }

    private fun loadTrips() {
        val userId = LocalStorage.getUserId(requireContext())
        val token = LocalStorage.getToken(requireContext())

        if (userId <= 0 || token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.getAllTrips(
            userId,
            "Bearer $token"
        ).enqueue(object : Callback<List<Trip>> {

            override fun onResponse(
                call: Call<List<Trip>>,
                response: Response<List<Trip>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    tripList.clear()
                    tripList.addAll(response.body()!!)
                    recyclerView.adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No trips found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "API error: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // ---------------- CODE-ONLY ADAPTER ----------------

    inner class TripAdapter :
        RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

        inner class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val fare: TextView
            val pickup: TextView
            val drop: TextView
            val distance: TextView

            init {
                val layout = view as LinearLayout

                fare = layout.getChildAt(0) as TextView
                pickup = layout.getChildAt(1) as TextView
                drop = layout.getChildAt(2) as TextView
                distance = layout.getChildAt(3) as TextView
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val context = parent.context

            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val fare = TextView(context).apply {
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.color_primary))
            }

            val pickup = TextView(context).apply {
                textSize = 14f
            }

            val drop = TextView(context).apply {
                textSize = 14f
            }

            val distance = TextView(context).apply {
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }

            container.addView(fare)
            container.addView(pickup)
            container.addView(drop)
            container.addView(distance)

            return TripViewHolder(container)
        }

        override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
            val trip = tripList[position]

            holder.fare.text = "₹${trip.finalFare}"
            holder.pickup.text = "Pickup: ${trip.pickupAddress}"
            holder.drop.text = "Drop: ${trip.dropAddress}"
            holder.distance.text = "Distance: ${trip.distanceText}"
        }

        override fun getItemCount(): Int = tripList.size
    }
}
