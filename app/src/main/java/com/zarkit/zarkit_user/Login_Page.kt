package com.zarkit.zarkit_user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zarkit.zarkit_user.databinding.ActivityLoginPageBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.net.Uri

class Login_Page : BaseActivity() {

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

            LocalStorage.savePhone(this, phone)
            callLoginApi(phone)
        }

        // ⭐ IMPORTANT — CALL THIS
        setupTermsAndPrivacyText()
    }


    private fun setupTermsAndPrivacyText() {

        val text = "By clicking Login you agree to the Terms of Services and Privacy Policy"

        val spannable = SpannableString(text)

        val termsStart = text.indexOf("Terms of Services")
        val termsEnd = termsStart + "Terms of Services".length

        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        // Terms Click
        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openLink("http://72.60.200.11/pdfs/terms_and_conditions_for_zarkit.pdf")
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = Color.parseColor("#1565C0")
                ds.isUnderlineText = false
                ds.bgColor = Color.TRANSPARENT
            }
        }

        // Privacy Click
        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openLink("http://72.60.200.11/pdfs/Privacy%20Policy%20for%20Zarkit.pdf")
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = Color.parseColor("#1565C0")
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(
            termsClickable,
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            privacyClickable,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.termsConditionsText.text = spannable
        binding.termsConditionsText.highlightColor = Color.TRANSPARENT
        binding.termsConditionsText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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
