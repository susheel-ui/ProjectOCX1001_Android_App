package com.example.project_a_android_userapp.Fragements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.project_a_android_userapp.LocalStorage
import com.example.project_a_android_userapp.Login_Page
import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.model.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Helper class to manage a label/value pair like First Name or Last Name
class UserField(private val valueTextView: TextView) {
    fun setText(text: String) {
        valueTextView.text = text
    }

    fun getText(): String {
        return valueTextView.text.toString()
    }
}

class UserFragment : Fragment() {

    // Main user info
    private lateinit var userNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var mobileText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var logoutButton: Button

    // First Name and Last Name fields using helper class
    private lateinit var firstNameField: UserField
    private lateinit var lastNameField: UserField

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // Bind main userNameText
        userNameText = view.findViewById(R.id.userNameText)

        // Bind First Name and Last Name TextViews
        firstNameField = UserField(view.findViewById(R.id.firstNameValue))
        lastNameField = UserField(view.findViewById(R.id.lastNameValue))

        emailText = view.findViewById(R.id.emailText)
        mobileText = view.findViewById(R.id.mobileText)
        profileImage = view.findViewById(R.id.profileImage)
        logoutButton = view.findViewById(R.id.logoutButton)

        fetchUserData()

        // Logout
        logoutButton.setOnClickListener {
            LocalStorage.clear(requireContext())
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), Login_Page::class.java)

            // Clear activity stack (IMPORTANT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        return view
    }

    private fun fetchUserData() {
        val userId = LocalStorage.getUserId(requireContext())

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.getUserById(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // Set main user name
                    userNameText.text = "${user.firstName} ${user.lastName}"

                    // Set First Name and Last Name fields
                    firstNameField.setText(user.firstName)
                    lastNameField.setText(user.lastName)

                    // Set email and mobile
                    emailText.text = user.email
                    mobileText.text = user.mobile
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
