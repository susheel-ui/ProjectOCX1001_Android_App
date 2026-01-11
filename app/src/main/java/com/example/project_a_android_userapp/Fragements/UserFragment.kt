package com.example.project_a_android_userapp.Fragements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project_a_android_userapp.LocalStorage
import com.example.project_a_android_userapp.Login_Page
import com.example.project_a_android_userapp.R
import com.example.project_a_android_userapp.api.ApiClient
import com.example.project_a_android_userapp.api.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPhone: TextView
    private lateinit var btnLogout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_user, container, false)

        txtName = view.findViewById(R.id.txtName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtPhone = view.findViewById(R.id.txtPhone)
        btnLogout = view.findViewById(R.id.btnLogout)

        loadUserProfile()

        btnLogout.setOnClickListener {
            LocalStorage.clear(requireContext())

            startActivity(
                Intent(requireContext(), Login_Page::class.java)
            )
            requireActivity().finish()
        }

        return view
    }

    private fun loadUserProfile() {
        val userId = LocalStorage.getUserId(requireContext())
        val token = LocalStorage.getToken(requireContext())

        if (userId <= 0 || token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.getUserDetails(
            userId,
            "Bearer $token"
        ).enqueue(object : Callback<UserProfileResponse> {

            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    val fullName = listOfNotNull(
                        user.firstName,
                        user.lastName
                    ).joinToString(" ")

                    txtName.text = if (fullName.isNotEmpty()) fullName else "User"
                    txtEmail.text = user.email ?: "Not available"
                    txtPhone.text = user.mobile ?: "Not available"
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "API error: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
