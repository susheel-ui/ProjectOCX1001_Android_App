package com.example.project_a_android_userapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.model.Booking

class BookingAdapter(
    private val bookingList: List<Booking>,
    private val onRebookClick: (rideId: Long) -> Unit,
    private val onInvoiceClick: (rideId: Long) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imgVehicle: ImageView = itemView.findViewById(R.id.imgVehicle)
        val tvVehicle: TextView = itemView.findViewById(R.id.tvVehicle)

        val tvPickup: TextView = itemView.findViewById(R.id.tvPickup)
        val tvDrop: TextView = itemView.findViewById(R.id.tvDrop)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvFare: TextView = itemView.findViewById(R.id.tvFare)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val btnRebook: Button = itemView.findViewById(R.id.btnRebook)
        val btnInvoice: Button = itemView.findViewById(R.id.btnInvoice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {

        val booking = bookingList[position]

        // BASIC DATA
        holder.tvPickup.text = booking.pickupAddress
        holder.tvDrop.text = booking.dropAddress
        holder.tvFare.text = "₹ ${booking.finalFare}"

        // DISTANCE (SHOW ONLY IF PRESENT)
        if (!booking.distanceText.isNullOrBlank()) {
            holder.tvDistance.visibility = View.VISIBLE
            holder.tvDistance.text = booking.distanceText
        } else {
            holder.tvDistance.visibility = View.GONE
        }

        // VEHICLE IMAGE + NAME (BASED ON API)
        when {
            booking.vehicleInfo.contains("TWO", ignoreCase = true) -> {
                holder.imgVehicle.setImageResource(R.drawable.v2w)
                holder.tvVehicle.text = "Two Wheeler"
            }

            booking.vehicleInfo.contains("THREE", ignoreCase = true) -> {
                holder.imgVehicle.setImageResource(R.drawable.v3w)
                holder.tvVehicle.text = "Three Wheeler"
            }

            booking.vehicleInfo.contains("FOUR", ignoreCase = true) -> {
                holder.imgVehicle.setImageResource(R.drawable.v4w)
                holder.tvVehicle.text = "Four Wheeler"
            }

            else -> {
                holder.imgVehicle.setImageResource(R.drawable.v4w)
                holder.tvVehicle.text = "Vehicle"
            }
        }

        // STATUS (TEXT ONLY)
        holder.tvStatus.text = booking.status

        when (booking.status) {
            "CANCELLED" -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        android.R.color.holo_red_dark
                    )
                )
                holder.btnInvoice.visibility = View.GONE
            }

            "COMPLETED" -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        android.R.color.holo_blue_dark
                    )
                )
                holder.btnInvoice.visibility = View.VISIBLE
            }

            else -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        android.R.color.darker_gray
                    )
                )
                holder.btnInvoice.visibility = View.GONE
            }
        }

        // CLICKS
        holder.btnRebook.setOnClickListener {
            onRebookClick(booking.rideId)
        }

        holder.btnInvoice.setOnClickListener {
            onInvoiceClick(booking.rideId)
        }
    }

    override fun getItemCount(): Int = bookingList.size
}
