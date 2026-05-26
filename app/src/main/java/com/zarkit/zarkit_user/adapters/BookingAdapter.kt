package com.zarkit.zarkit_user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zarkit.zarkit_user.R
import com.zarkit.zarkit_user.model.Booking

class BookingAdapter(
    private val bookingList: MutableList<Booking>,
    private val onRebookClick: (rideId: Long) -> Unit,
    private val onInvoiceClick: (rideId: Long) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgVehicle: ImageView = itemView.findViewById(R.id.imgVehicle)
        val tvVehicle: TextView   = itemView.findViewById(R.id.tvVehicle)
        val tvPickup: TextView    = itemView.findViewById(R.id.tvPickup)
        val tvDrop: TextView      = itemView.findViewById(R.id.tvDrop)
        val tvFare: TextView      = itemView.findViewById(R.id.tvFare)
        val tvStatus: TextView    = itemView.findViewById(R.id.tvStatus)
        val btnRebook: Button     = itemView.findViewById(R.id.btnRebook)
        val btnInvoice: Button    = itemView.findViewById(R.id.btnInvoice)

        val tvRideId: TextView = itemView.findViewById(R.id.tvRideId)
    }

    // ================= CREATE =================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    // ================= BIND =================
    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        // ── BASIC DATA ──────────────────────────────────
        holder.tvRideId.text = "Ride ID: Zarkit_OCX_${booking.rideId}"
        holder.tvPickup.text = booking.pickupAddress
        holder.tvDrop.text   = booking.dropAddress
        holder.tvFare.text   = "₹ ${booking.finalFare}"

        // ── VEHICLE IMAGE + NAME ────────────────────────
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

        // ── STATUS ──────────────────────────────────────
        holder.tvStatus.text = booking.status

        when (booking.status.uppercase()) {
            "CANCELLED" -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
                )
                holder.btnInvoice.visibility = View.GONE
            }
            "COMPLETED" -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_dark)
                )
                holder.btnInvoice.visibility = View.VISIBLE
            }
            else -> {
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
                )
                holder.btnInvoice.visibility = View.GONE
            }
        }

        // ── CLICKS ──────────────────────────────────────
        holder.btnRebook.setOnClickListener {
            onRebookClick(booking.rideId)
        }
        holder.btnInvoice.setOnClickListener {
            onInvoiceClick(booking.rideId)
        }
    }

    // ================= COUNT =================
    override fun getItemCount(): Int = bookingList.size

    // ================= UPDATE LIST (for tab/search filter) =================
    fun updateList(newList: List<Booking>) {
        bookingList.clear()
        bookingList.addAll(newList)
        notifyDataSetChanged()
    }
}