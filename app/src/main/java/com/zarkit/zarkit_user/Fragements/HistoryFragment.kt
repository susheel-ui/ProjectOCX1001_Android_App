package com.zarkit.zarkit_user.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zarkit.zarkit_user.FareActivity
import com.zarkit.zarkit_user.R
import com.zarkit.zarkit_user.adapter.BookingAdapter
import com.zarkit.zarkit_user.api.ApiClient
import com.zarkit.zarkit_user.LocalStorage
import com.zarkit.zarkit_user.model.Booking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    // ── Views ──────────────────────────────────────────────
    private var _rvBookings: RecyclerView? = null
    private var _illustrationImage: ImageView? = null
    private var _textNoHistory: TextView? = null
    private var _textSubMessage: TextView? = null
    private var _infoBannerCard: CardView? = null

    // Tabs
    private var _tabAll: TextView? = null
    private var _tabCompleted: TextView? = null
    private var _tabCancelled: TextView? = null

    private val rvBookings get() = _rvBookings
    private val illustrationImage get() = _illustrationImage
    private val textNoHistory get() = _textNoHistory
    private val textSubMessage get() = _textSubMessage

    // ── Data ───────────────────────────────────────────────
    private var bookingAdapter: BookingAdapter? = null

    /** Master list — never filtered */
    private var allBookings: List<Booking> = emptyList()

    /** Currently active tab: ALL | COMPLETED | CANCELLED */
    private var activeTab = TAB_ALL

    companion object {
        const val TAB_ALL           = "ALL"
        const val TAB_COMPLETED     = "COMPLETED"
        const val TAB_CANCELLED     = "CANCELLED"
    }

    // ── Lifecycle ──────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        bindViews(view)
        setupTabs()
        setupInfoBanner()

        _rvBookings?.layoutManager = LinearLayoutManager(requireContext())

        fetchBookingHistory()
        return view
    }

    // ── View Binding ───────────────────────────────────────
    private fun bindViews(view: View) {
        _rvBookings        = view.findViewById(R.id.rvBookings)
        _illustrationImage = view.findViewById(R.id.illustration_image)
        _textNoHistory     = view.findViewById(R.id.text_no_history)
        _textSubMessage    = view.findViewById(R.id.text_sub_message)
        _infoBannerCard    = view.findViewById(R.id.infoBannerCard)
        _tabAll            = view.findViewById(R.id.tabAll)
        _tabCompleted      = view.findViewById(R.id.tabCompleted)
        _tabCancelled      = view.findViewById(R.id.tabCancelled)
    }

    // ── Info Banner ────────────────────────────────────────
    private fun setupInfoBanner() {
        _infoBannerCard?.findViewById<ImageView>(R.id.btnDismissBanner)
            ?.setOnClickListener {
                _infoBannerCard?.visibility = View.GONE
            }
    }

    // ── Tabs ───────────────────────────────────────────────
    private fun setupTabs() {
        _tabAll?.setOnClickListener         { switchTab(TAB_ALL) }
        _tabCompleted?.setOnClickListener   { switchTab(TAB_COMPLETED) }
        _tabCancelled?.setOnClickListener   { switchTab(TAB_CANCELLED) }
    }

    private fun switchTab(tab: String) {
        activeTab = tab
        updateTabUI(tab)
        applyFilter()
    }

    private fun updateTabUI(activeTabKey: String) {
        val tabs = mapOf(
            TAB_ALL          to _tabAll,
            TAB_COMPLETED    to _tabCompleted,
            TAB_CANCELLED    to _tabCancelled
        )
        tabs.forEach { (key, tv) ->
            if (key == activeTabKey) {
                tv?.setBackgroundResource(R.drawable.tab_selected_bg)
                tv?.setTextColor(resources.getColor(android.R.color.black, null))
                tv?.textSize = 13f
                tv?.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                tv?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                tv?.setTextColor(android.graphics.Color.parseColor("#888888"))
                tv?.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    // ── Filter Logic ───────────────────────────────────────
    private fun applyFilter() {
        val filtered = when (activeTab) {
            TAB_COMPLETED    -> allBookings.filter { it.status.equals("COMPLETED", true) }
            TAB_CANCELLED    -> allBookings.filter { it.status.equals("CANCELLED", true) }
            else             -> allBookings
        }
        renderList(filtered)
    }

    // ── Render ─────────────────────────────────────────────
    private fun renderList(list: List<Booking>) {
        if (!isAdded || view == null) return

        if (list.isNotEmpty()) {
            rvBookings?.visibility        = View.VISIBLE
            illustrationImage?.visibility = View.GONE
            textNoHistory?.visibility     = View.GONE
            textSubMessage?.visibility    = View.GONE

            if (bookingAdapter == null) {
                bookingAdapter = BookingAdapter(
                    list.toMutableList(),
                    onRebookClick  = { rideId -> rebookRide(rideId) },
                    onInvoiceClick = { rideId -> downloadInvoice(rideId) }
                )
                rvBookings?.adapter = bookingAdapter
            } else {
                bookingAdapter?.updateList(list)
            }
        } else {
            showEmptyState()
        }
    }

    // ── API ────────────────────────────────────────────────
    private fun fetchBookingHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            try {
                val userId   = withContext(Dispatchers.IO) { LocalStorage.getUserId(ctx) }
                val response = withContext(Dispatchers.IO) { ApiClient.api.getAllBookings(userId) }

                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    allBookings = response.body()!!
                    applyFilter()
                } else {
                    showEmptyState()
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Exception: ${e.localizedMessage}")
                if (isAdded && view != null) showEmptyState()
            }
        }
    }

    private fun rebookRide(rideId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            try {
                val token = withContext(Dispatchers.IO) { LocalStorage.getToken(ctx) } ?: run {
                    if (isAdded) Toast.makeText(ctx, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = withContext(Dispatchers.IO) { ApiClient.api.bookAgain("Bearer $token", rideId) }
                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val booking = response.body()!!

                    // LocalStorage mein rebook data save karo
                    withContext(Dispatchers.IO) {
                        LocalStorage.savePickupLocation(
                            ctx,
                            lat     = booking.pickupLat,
                            lng     = booking.pickupLon,
                            address = booking.pickupAddress
                        )
                        LocalStorage.saveDropLocation(
                            ctx,
                            lat     = booking.dropLat,
                            lng     = booking.dropLon,
                            address = booking.dropAddress
                        )
                        LocalStorage.saveDistanceAndDuration(
                            ctx,
                            distanceText  = booking.distanceText,
                            durationText  = booking.durationText,
                            distanceValue = booking.distanceValue,
                            durationValue = booking.durationValue
                        )
                        LocalStorage.saveSenderDetails(
                            ctx,
                            house = booking.senderHouse,
                            name  = booking.senderName,
                            phone = booking.senderPhone,
                            type  = booking.senderType
                        )
                        LocalStorage.saveReceiverDetails(
                            ctx,
                            house = booking.receiverHouse,
                            name  = booking.receiverName,
                            phone = booking.receiverPhone,
                            type  = booking.receiverType
                        )
                    }

                    startActivity(Intent(ctx, FareActivity::class.java))
                } else {
                    Toast.makeText(ctx, "Unable to rebook. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("REBOOK", "Rebook failed", e)
                if (isAdded) Toast.makeText(ctx, "Something went wrong.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadInvoice(rideId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            try {
                val token = withContext(Dispatchers.IO) { LocalStorage.getToken(ctx) } ?: run {
                    if (isAdded) Toast.makeText(ctx, "Session expired.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val response = withContext(Dispatchers.IO) { ApiClient.api.downloadInvoicePdf("Bearer $token", rideId) }
                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val file = java.io.File(ctx.getExternalFilesDir(null), "invoice_$rideId.pdf")
                    file.writeBytes(response.body()!!.bytes())
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        ctx, "${ctx.packageName}.provider", file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(ctx, "Invoice not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("INVOICE", "Invoice download failed", e)
                if (isAdded) Toast.makeText(ctx, "Unable to open invoice", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEmptyState() {
        if (!isAdded || view == null) return
        rvBookings?.visibility        = View.GONE
        illustrationImage?.visibility = View.VISIBLE
        textNoHistory?.visibility     = View.VISIBLE
        textSubMessage?.visibility    = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bookingAdapter   = null
        _rvBookings      = null
        _illustrationImage = null
        _textNoHistory   = null
        _textSubMessage  = null
        _infoBannerCard  = null
        _tabAll          = null
        _tabCompleted    = null
        _tabCancelled    = null
    }
}