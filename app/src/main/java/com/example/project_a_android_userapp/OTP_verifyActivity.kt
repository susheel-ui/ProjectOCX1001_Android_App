package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.VerifyOtpBody
import com.example.project_a_android_userapp.databinding.ActivityOtpVerifyBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // ✅ Get phone from LocalStorage
        val phone = LocalStorage.getPhone(this)

        if (phone.isNullOrEmpty()) {
            Toast.makeText(this, "Phone missing. Login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Show phone number in UI
        binding.phoneNumberText.text = phone

        // ✅ Change number → back to Login
        binding.changeButton.setOnClickListener {
            val intent = Intent(this, Login_Page::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // ✅ Verify OTP
        binding.verifyButton.setOnClickListener {

            val otp = binding.otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prevent double clicks
            binding.verifyButton.isEnabled = false

            verifyOtpApi(phone, otp)
        }
    }

    private fun verifyOtpApi(mobile: String, otp: String) {

        val body = VerifyOtpBody(mobile, otp)

        ApiClient.api.verifyOtp(body).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                binding.verifyButton.isEnabled = true

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@OTP_verifyActivity,
                        "Invalid OTP",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val bodyStr = response.body()?.string()
                if (bodyStr.isNullOrEmpty()) {
                    Toast.makeText(
                        this@OTP_verifyActivity,
                        "Empty server response",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val json = JSONObject(bodyStr)

                val code = json.optString("code")
                val token = json.optString("token")
                val role = json.optString("role")
                val userId = json.optInt("userId", -1)

                if (code == "LOGIN_SUCCESS") {

                    val safeRole = role.trim().uppercase()

                    if (safeRole != "USER") {
                        Toast.makeText(
                            this@OTP_verifyActivity,
                            "Only users are allowed to login",
                            Toast.LENGTH_LONG
                        ).show()

                        return
                    }

                    // ✅ Save login data
                    LocalStorage.saveToken(this@OTP_verifyActivity, token)
                    LocalStorage.saveRole(this@OTP_verifyActivity, role)

                    if (userId != -1) {
                        LocalStorage.saveUserId(this@OTP_verifyActivity, userId)
                    }

                    Toast.makeText(
                        this@OTP_verifyActivity,
                        "OTP Verified",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(this@OTP_verifyActivity, Home_Activity::class.java)
                    )
                    finish()

                } else {
                    Toast.makeText(
                        this@OTP_verifyActivity,
                        "OTP Invalid",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.verifyButton.isEnabled = true
                Toast.makeText(
                    this@OTP_verifyActivity,
                    "Network Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
