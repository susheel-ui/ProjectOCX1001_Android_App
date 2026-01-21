package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_a_android_userapp.databinding.ActivityLoginPageBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Login_Page : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString().trim()

            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save phone first
            LocalStorage.savePhone(this, phone)

            // Call login API
            callLoginApi(phone)
        }
    }

    private fun callLoginApi(phone: String) {

        val json = """{"mobile":"$phone"}"""
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://72.60.200.11:8080/auth/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Login_Page, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val rawBody = response.body?.string()
                val json = if (!rawBody.isNullOrEmpty()) JSONObject(rawBody) else JSONObject()

                runOnUiThread {

                    val code = json.optString("code")

                    when (response.code) {

                        404 -> {   // NEED REGISTER
                            if (code == "NEED_REGISTER") {
                                startActivity(Intent(this@Login_Page, ResistrationActivity::class.java))
                            } else {
                                Toast.makeText(this@Login_Page, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        }

                        200 -> {   // OTP SENT
                            if (code == "OTP_SENT") {
                                startActivity(Intent(this@Login_Page, OTP_verifyActivity::class.java))
                            } else {
                                Toast.makeText(this@Login_Page, "Unexpected Response", Toast.LENGTH_SHORT).show()
                            }
                        }

                        else -> {
                            Toast.makeText(
                                this@Login_Page,
                                "Unexpected Error: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }
}
