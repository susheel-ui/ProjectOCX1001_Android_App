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

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Your handler stays, only logic added
        Handler(Looper.getMainLooper()).postDelayed({

            val token = LocalStorage.getToken(this)

            val intent = if (!token.isNullOrEmpty()) {
                //  User logged in
                Intent(this, Home_Activity::class.java)
            } else {
                //  User not logged in
                Intent(this, Login_Page::class.java)
            }

            startActivity(intent)
            finish()

        }, 0) // ⬅ No delay (instant)
    }
}
