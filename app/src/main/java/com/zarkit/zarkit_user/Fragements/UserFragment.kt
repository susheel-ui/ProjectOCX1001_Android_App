package com.zarkit.zarkit_user.Fragements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.zarkit.zarkit_user.LocalStorage
import com.zarkit.zarkit_user.Login_Page
import com.zarkit.zarkit_user.PoliciesActivity
import com.zarkit.zarkit_user.R
import com.zarkit.zarkit_user.api.ApiClient
import com.zarkit.zarkit_user.api.UpdateUserRequest
import com.zarkit.zarkit_user.model.UserResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserField(private val valueTextView: TextView) {
    fun setText(text: String) { valueTextView.text = text }
    fun getText(): String = valueTextView.text.toString()
}

class UserFragment : Fragment() {

    private var userNameText: TextView? = null
    private var emailText: TextView? = null
    private var mobileText: TextView? = null
    private var logoutButton: Button? = null
    private var btnEditProfile: Button? = null
    private var tvTermsPolicies: TextView? = null
    private var tvVersion: TextView? = null

    private var firstNameField: UserField? = null
    private var lastNameField: UserField? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        userNameText = view.findViewById(R.id.userNameText)
        emailText = view.findViewById(R.id.emailText)
        mobileText = view.findViewById(R.id.mobileText)
        logoutButton = view.findViewById(R.id.logoutButton)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        tvTermsPolicies = view.findViewById(R.id.tvTermsPolicies)
        tvVersion = view.findViewById(R.id.tvVersion)

        firstNameField = UserField(view.findViewById(R.id.firstNameValue))
        lastNameField = UserField(view.findViewById(R.id.lastNameValue))

        // ✅ Set version dynamically from build.gradle versionName
        setAppVersion()

        fetchUserData()

        btnEditProfile?.setOnClickListener {
            openEditProfileBottomSheet()
        }

        logoutButton?.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            LocalStorage.clear(ctx)
            Toast.makeText(ctx, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(ctx, Login_Page::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        tvTermsPolicies?.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            startActivity(Intent(ctx, PoliciesActivity::class.java))
        }

        return view
    }


    // ================= VERSION =================

    private fun setAppVersion() {
        val ctx = context ?: return
        try {
            // ✅ Reads versionName from your build.gradle automatically
            val versionName = ctx.packageManager
                .getPackageInfo(ctx.packageName, 0)
                .versionName
            tvVersion?.text = "Zarkit • v$versionName\n© 2026 Zarkit Group Pvt Ltd. All rights reserved."
        } catch (e: Exception) {
            tvVersion?.text = "Zarkit\n© 2026 Zarkit Group Pvt Ltd. All rights reserved."
        }
    }


    // ================= FETCH USER =================

    private fun fetchUserData() {
        val ctx = context ?: return
        val userId = LocalStorage.getUserId(ctx)

        if (userId == -1) {
            Toast.makeText(ctx, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.getUserById(userId)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (!isAdded || view == null) return
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        userNameText?.text = "${user.firstName} ${user.lastName}"
                        firstNameField?.setText(user.firstName)
                        lastNameField?.setText(user.lastName)
                        emailText?.text = user.email
                        mobileText?.text = user.mobile
                    } else {
                        if (isAdded) Toast.makeText(ctx, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(ctx, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    // ================= EDIT BOTTOM SHEET =================

    private fun openEditProfileBottomSheet() {
        val ctx = context ?: return
        val dialog = BottomSheetDialog(ctx)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_edit_profile, null)

        val editFirstName = sheetView.findViewById<EditText>(R.id.editFirstName)
        val editLastName = sheetView.findViewById<EditText>(R.id.editLastName)
        val editEmail = sheetView.findViewById<EditText>(R.id.editEmail)
        val btnSubmit = sheetView.findViewById<Button>(R.id.btnSubmitProfile)

        editFirstName.setText(firstNameField?.getText() ?: "")
        editLastName.setText(lastNameField?.getText() ?: "")
        editEmail.setText(emailText?.text?.toString() ?: "")

        btnSubmit.setOnClickListener {
            val newFirst = editFirstName.text.toString().trim()
            val newLast = editLastName.text.toString().trim()
            val newEmail = editEmail.text.toString().trim()

            if (newFirst.isEmpty() || newLast.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(ctx, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(ctx, "Enter valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isAdded && view != null) {
                firstNameField?.setText(newFirst)
                lastNameField?.setText(newLast)
                emailText?.text = newEmail
                userNameText?.text = "$newFirst $newLast"
            }

            updateUserProfile(newFirst, newLast, newEmail)
            dialog.dismiss()
        }

        dialog.setContentView(sheetView)
        if (isAdded && !requireActivity().isFinishing) dialog.show()
    }


    // ================= UPDATE API =================

    private fun updateUserProfile(firstName: String, lastName: String, email: String) {
        val ctx = context ?: return
        val userId = LocalStorage.getUserId(ctx)
        val token = LocalStorage.getToken(ctx)

        if (userId == -1 || token.isNullOrEmpty()) {
            Toast.makeText(ctx, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateUserRequest(firstName = firstName, lastName = lastName, email = email)

        ApiClient.api.updateUser(id = userId, token = "Bearer $token", request = request)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        Toast.makeText(ctx, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(ctx, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    // ================= CLEANUP =================

    override fun onDestroyView() {
        super.onDestroyView()
        userNameText = null
        emailText = null
        mobileText = null
        logoutButton = null
        btnEditProfile = null
        tvTermsPolicies = null
        tvVersion = null
        firstNameField = null
        lastNameField = null
    }
}