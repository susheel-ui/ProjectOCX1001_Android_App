package com.zarkit.zarkit_user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zarkit.zarkit_user.databinding.ActivityBulkOrderBinding

class BulkOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBulkOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBulkOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // ================= Toolbar Setup =================
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)  // Enable back arrow
            title = "Bulk Orders"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish() // Close this activity when back arrow clicked
        }

        // ================= Contact Card Click =================
        binding.contactCard.setOnClickListener {
            // Open email app with support@zarkit.com
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@zarkit.com")
                putExtra(Intent.EXTRA_SUBJECT, "Bulk Order Inquiry")
            }

            // Ensure there is an email client
            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(emailIntent)
            }
        }
    }
}
