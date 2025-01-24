/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.viewmodel

import android.app.Activity
import android.content.SharedPreferences.Editor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val editor: Editor,
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val phoneNumber = MutableLiveData<String>()
    val phoneNumberError = MutableLiveData<String?>()

    val isRequestOtpSuccessful = MutableLiveData<Boolean>()
    val isRequestOtpApiCall = MutableLiveData<Boolean>()

    // Method to update field
    fun setPhoneNumber(newPhone: CharSequence, start: Int, before: Int, count: Int) {
        phoneNumber.value = newPhone.toString()
        phoneNumberError.value = validateUKPhoneNumber(newPhone.toString())
    }

    // Method to handle error field
    private fun validateUKPhoneNumber(phoneNumber: String): String? {
        return when {
            phoneNumber.isBlank() -> "Phone number is required"
            phoneNumber.length != 11 -> "Phone number must be 11 digits long"
            !phoneNumber.startsWith("7") -> "Phone number must start with 7"
            !phoneNumber.matches(Regex("^[0-9]+\$")) -> "Phone number must contain only digits"
            else -> null
        }
    }

    // Method to handle the button click
    fun onButtonClicked() {
        val allFieldsValid = !(phoneNumber.value.isNullOrBlank())

        if(allFieldsValid) {
            if (phoneNumberError.value == null) {
                isRequestOtpApiCall.value = true
            } else {
                isRequestOtpApiCall.value = false
            }
        } else {
            phoneNumberError.value = "Please enter a valid phone number"
            isRequestOtpApiCall.value = false
        }
    }

    fun callRequestOtpApi(requireActivity: Activity) {

    }
}