package com.example.project_a_android_userapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.model.Booking

class BookingAdapter(private val bookingList: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvPickup: TextView = itemView.findViewById(R.id.tvPickup)
        val tvDrop: TextView = itemView.findViewById(R.id.tvDrop)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvFare: TextView = itemView.findViewById(R.id.tvFare)

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

        holder.tvPickup.text = "Pickup: ${booking.pickupAddress}"
        holder.tvDrop.text = "Drop: ${booking.dropAddress}"
        holder.tvDistance.text = "Distance: ${booking.distanceText}"
        holder.tvFare.text = "₹ ${booking.finalFare}"

        // Buttons currently DO NOTHING (as you said)

        holder.btnRebook.setOnClickListener {
            // Leave empty for now
        }

        holder.btnInvoice.setOnClickListener {
            // Leave empty for now
        }
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}
