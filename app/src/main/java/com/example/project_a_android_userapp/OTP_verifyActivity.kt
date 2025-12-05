package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_a_android_userapp.databinding.ActivityOtpVerifyBinding

class OTP_verifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // -----------------------------------------
        // 🔥 Load stored phone (NOT shown to user)
        // -----------------------------------------
        val phone = LocalStorage.getPhone(this)

        if (phone.isNullOrBlank()) {
            Toast.makeText(this, "Phone missing, login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // -----------------------------------------
        // OTP Verify Button Click
        // -----------------------------------------
        binding.verifyButton.setOnClickListener {

            val otp = binding.otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Here you will send OTP + phone to backend:
            // verifyOtp(phone, otp)

            // For now just go to Home Screen
            val intent = Intent(this, Home_Activity::class.java)
            startActivity(intent)
        }
    }
}
