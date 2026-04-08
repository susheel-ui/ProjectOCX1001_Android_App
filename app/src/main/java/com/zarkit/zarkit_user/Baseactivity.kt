package com.zarkit.zarkit_user

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private lateinit var networkMonitor: NetworkMonitor
    private var noInternetDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkMonitor = NetworkMonitor(this)
    }

    override fun onStart() {
        super.onStart()
        networkMonitor.isConnected.observe(this) { isConnected ->
            if (isConnected) {
                // Internet back — auto dismiss
                if (noInternetDialog?.isShowing == true) {
                    noInternetDialog?.dismiss()
                }
            } else {
                showNoInternetSheet()
            }
        }
        networkMonitor.start()
    }

    override fun onStop() {
        super.onStop()
        networkMonitor.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        noInternetDialog?.dismiss()
        noInternetDialog = null
    }

    private fun showNoInternetSheet() {
        if (isFinishing || isDestroyed) return
        if (noInternetDialog?.isShowing == true) return

        noInternetDialog = Dialog(this)
        noInternetDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.bottom_sheet_no_internet)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(Gravity.BOTTOM)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            show()
        }
    }
}