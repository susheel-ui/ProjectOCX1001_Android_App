package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.RegisterBody
import com.example.project_a_android_userapp.databinding.ActivityResistrationBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityResistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ MOBILE FROM LOCAL STORAGE
        val mobile = LocalStorage.getPhone(this)

        if (mobile.isNullOrEmpty()) {
            Toast.makeText(this, "Phone missing. Login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.mobileNo.text = mobile

        // ✅ CHANGE NUMBER
        binding.tvChange.setOnClickListener {
            val intent = Intent(this, Login_Page::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // ✅ REGISTER BUTTON
        binding.buttonNext.setOnClickListener {

            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()

            if (firstName.isEmpty()) {
                Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            callRegisterApi(
                mobile = mobile,
                firstName = firstName,
                lastName = lastName.ifEmpty { null },
                email = email.ifEmpty { null }
            )
        }
    }

    private fun callRegisterApi(
        mobile: String,
        firstName: String,
        lastName: String?,
        email: String?
    ) {

        binding.buttonNext.isEnabled = false

        val body = RegisterBody(
            mobile = mobile,
            firstName = firstName,
            lastName = lastName,
            email = email
        )

        ApiClient.api.register(body).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                binding.buttonNext.isEnabled = true

                val raw = response.body()?.string()
                val error = response.errorBody()?.string()

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@ResistrationActivity,
                        "Register failed: $error",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if (raw.isNullOrEmpty()) {
                    Toast.makeText(
                        this@ResistrationActivity,
                        "Empty server response",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val json = JSONObject(raw)
                val code = json.optString("code")

                when (code) {

                    "REGISTER_OTP_SENT",
                    "OTP_SENT" -> {
                        startActivity(
                            Intent(this@ResistrationActivity, OTP_verifyActivity::class.java)
                        )
                        finish()
                    }

                    else -> {
                        Toast.makeText(
                            this@ResistrationActivity,
                            "Register failed: $code",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.buttonNext.isEnabled = true
                Toast.makeText(
                    this@ResistrationActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
