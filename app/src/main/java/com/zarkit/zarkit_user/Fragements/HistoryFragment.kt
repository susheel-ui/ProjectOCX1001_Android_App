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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zarkit.zarkit_user.FareActivity
import com.zarkit.zarkit_user.LocationViewModel
import com.zarkit.zarkit_user.MyApp
import com.zarkit.zarkit_user.R
import com.zarkit.zarkit_user.adapter.BookingAdapter
import com.zarkit.zarkit_user.api.ApiClient
import com.zarkit.zarkit_user.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private var _rvBookings: RecyclerView? = null
    private var _illustrationImage: ImageView? = null
    private var _textNoHistory: TextView? = null
    private var _textSubMessage: TextView? = null

    // ✅ Safe accessors — null after onDestroyView
    private val rvBookings get() = _rvBookings
    private val illustrationImage get() = _illustrationImage
    private val textNoHistory get() = _textNoHistory
    private val textSubMessage get() = _textSubMessage

    private var bookingAdapter: BookingAdapter? = null
    private lateinit var vm: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        _rvBookings = view.findViewById(R.id.rvBookings)
        _illustrationImage = view.findViewById(R.id.illustration_image)
        _textNoHistory = view.findViewById(R.id.text_no_history)
        _textSubMessage = view.findViewById(R.id.text_sub_message)

        _rvBookings?.layoutManager = LinearLayoutManager(requireContext())

        vm = (requireActivity().application as MyApp).vm

        fetchBookingHistory()

        return view
    }

    private fun fetchBookingHistory() {

        // ✅ Use viewLifecycleOwner.lifecycleScope so coroutine auto-cancels when view is destroyed
        viewLifecycleOwner.lifecycleScope.launch {

            // ✅ Safe context capture — returns early if fragment detached
            val ctx = context ?: return@launch

            try {
                val userId = withContext(Dispatchers.IO) {
                    LocalStorage.getUserId(ctx)
                }

                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.getAllBookings(userId)
                }

                // ✅ Guard after background work completes
                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {

                    val bookingList = response.body()!!

                    if (bookingList.isNotEmpty()) {

                        rvBookings?.visibility = View.VISIBLE
                        illustrationImage?.visibility = View.GONE
                        textNoHistory?.visibility = View.GONE
                        textSubMessage?.visibility = View.GONE

                        bookingAdapter = BookingAdapter(
                            bookingList,
                            onRebookClick = { rideId ->
                                rebookRide(rideId)
                            },
                            onInvoiceClick = { rideId ->
                                downloadInvoice(rideId)
                            }
                        )

                        rvBookings?.adapter = bookingAdapter

                    } else {
                        showEmptyState()
                    }

                } else {
                    showEmptyState()
                }

            } catch (e: Exception) {
                Log.e("HistoryFragment", "Exception: ${e.localizedMessage}")

                // ✅ Only update UI if still attached
                if (isAdded && view != null) {
                    showEmptyState()
                }
            }
        }
    }

    private fun rebookRide(rideId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            // ✅ Safe context capture
            val ctx = context ?: return@launch

            try {
                val token = withContext(Dispatchers.IO) {
                    LocalStorage.getToken(ctx)
                } ?: run {
                    // ✅ Show error only if still attached
                    if (isAdded) {
                        Toast.makeText(ctx, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.bookAgain("Bearer $token", rideId)
                }

                // ✅ Guard after background work
                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    vm.setFromRebookResponse(response.body()!!)
                    startActivity(Intent(ctx, FareActivity::class.java))
                } else {
                    Toast.makeText(ctx, "Unable to rebook. Please try again.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("REBOOK", "Rebook failed", e)

                if (isAdded) {
                    Toast.makeText(ctx, "Something went wrong.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadInvoice(rideId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val ctx = context ?: return@launch

            try {
                val token = withContext(Dispatchers.IO) {
                    LocalStorage.getToken(ctx)
                } ?: run {
                    if (isAdded) {
                        Toast.makeText(ctx, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.downloadInvoicePdf("Bearer $token", rideId)
                }

                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {

                    val pdfBytes = response.body()!!.bytes()

                    val fileName = "invoice_$rideId.pdf"

                    val file = java.io.File(ctx.getExternalFilesDir(null), fileName)
                    file.writeBytes(pdfBytes)

                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        ctx,
                        "${ctx.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    startActivity(intent)

                } else {
                    Toast.makeText(ctx, "Invoice not found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("INVOICE", "Invoice download failed", e)

                if (isAdded) {
                    Toast.makeText(ctx, "Unable to open invoice", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEmptyState() {
        // ✅ Guard against calling on detached fragment
        if (!isAdded || view == null) return

        rvBookings?.visibility = View.GONE
        illustrationImage?.visibility = View.VISIBLE
        textNoHistory?.visibility = View.VISIBLE
        textSubMessage?.visibility = View.VISIBLE
    }

    // ✅ Nullify view references to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        bookingAdapter = null
        _rvBookings = null
        _illustrationImage = null
        _textNoHistory = null
        _textSubMessage = null
    }
}