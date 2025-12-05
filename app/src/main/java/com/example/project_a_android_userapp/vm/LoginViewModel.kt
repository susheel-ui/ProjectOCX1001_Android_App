package com.example.project_a_android_userapp.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    val phoneNumber = MutableLiveData<String>()
}
