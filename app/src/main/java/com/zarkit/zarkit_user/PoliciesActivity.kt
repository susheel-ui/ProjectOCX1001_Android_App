package com.zarkit.zarkit_user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PoliciesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_policies)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.btnBack)
        val about = findViewById<TextView>(R.id.tvAbout)
        val privacy = findViewById<TextView>(R.id.tvPrivacy)
        val terms = findViewById<TextView>(R.id.tvTerms)

        back.setOnClickListener {
            finish()
        }

        about.setOnClickListener {
            openLink("http://72.60.200.11/pdfs/about_us_zarkit.pdf")
        }

        privacy.setOnClickListener {
            openLink("http://72.60.200.11/pdfs/Privacy%20Policy%20for%20Zarkit.pdf")
        }

        terms.setOnClickListener {
            openLink("http://72.60.200.11/pdfs/terms_and_conditions_for_zarkit.pdf")
        }
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
