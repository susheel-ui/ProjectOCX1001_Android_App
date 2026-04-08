package com.zarkit.zarkit_user

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.zarkit.zarkit_user.Fragements.HomeFragment
import com.zarkit.zarkit_user.Fragements.PaymentFragment
import com.zarkit.zarkit_user.Fragements.UserFragment
import com.zarkit.zarkit_user.Fragments.HistoryFragment
import com.zarkit.zarkit_user.databinding.ActivityHomeBinding

class Home_Activity : BaseActivity() {
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainerViewTag.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                0
            )
            binding.bottomNavigation.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        try {
            changeFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        } catch (e: Exception) {
            Log.d(TAG, "onCreate: error ${e.message}")
        }
     binding.bottomNavigation.setOnItemSelectedListener {
         when (it.itemId) {
             R.id.nav_home -> {
                 changeFragment(HomeFragment())
             }
             R.id.nav_orders -> {
                 changeFragment(HistoryFragment())
             }
             R.id.nav_payments -> {
                 changeFragment(PaymentFragment())
             }
             R.id.nav_account -> {
                 changeFragment(UserFragment())
             }
             else -> {
                false
             }
         }
     }

    }
    fun changeFragment(fragment:Fragment):Boolean{
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerViewTag.id, fragment)
            .commit()
        return true
    }
}