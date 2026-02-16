package com.example.project_a_android_userapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_a_android_userapp.databinding.ActivityMainBinding

class MainActivity_FlashScreen : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Enable edge-to-edge drawing
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Apply system bar padding (status bar + navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // 🔹 Check login token and navigate
        Handler(Looper.getMainLooper()).postDelayed({

            val token = LocalStorage.getToken(this)

            val intent = if (!token.isNullOrEmpty()) {
                //  User logged in → go to Home
                Intent(this, Home_Activity::class.java)
            } else {
                // ❌ User not logged in → go to Login
                Intent(this, Login_Page::class.java)
            }

            startActivity(intent)
            finish()

        }, 0)
    }
}
