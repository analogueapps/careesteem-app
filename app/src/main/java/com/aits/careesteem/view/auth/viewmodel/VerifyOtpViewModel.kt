/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.viewmodel

import android.annotation.SuppressLint
import android.content.SharedPreferences.Editor
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val editor: Editor,
): ViewModel(){

    // LiveData for OTP input and error message
    val otp = MutableLiveData<String>()
    val otpError = MutableLiveData<String?>()

    // LiveData to track OTP validation success and API call state
    val isOtpValid = MutableLiveData<Boolean>()
    val isOtpApiCall = MutableLiveData<Boolean>()

    // LiveData for timer text and visibility states
    private val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String> get() = _timerText

    private val _timerVisible = MutableLiveData<Boolean>().apply { value = true }
    val timerVisible: LiveData<Boolean> get() = _timerVisible

    private val _resendVisible = MutableLiveData<Boolean>().apply { value = false }
    val resendVisible: LiveData<Boolean> get() = _resendVisible

    private var timer: CountDownTimer? = null

    init {
        startTimer() // Start the countdown timer when the ViewModel is created
    }

    // Method to update OTP field
    fun setOtp(newOtp: CharSequence, start: Int, before: Int, count: Int) {
        otp.value = newOtp.toString()
        otpError.value = validateOtp(newOtp.toString())
    }

    // Method to validate OTP
    private fun validateOtp(otp: String): String? {
        return when {
            otp.isBlank() -> "OTP is required"
            otp.length != 6 -> "OTP must be 6 digits long"
            !otp.matches(Regex("^[0-9]+\$")) -> "OTP must contain only digits"
            else -> null
        }
    }

    // Method to handle OTP verification
    fun onVerifyOtp() {
        val otpValue = otp.value
        if (otpValue == null) {
            otpError.value = "OTP is required"
        } else {
            otpError.value = validateOtp(otpValue)
        }
        isOtpValid.value = otpValue != null && validateOtp(otpValue) == null
    }

    // Method to handle resend OTP
    fun onResendOtp() {
        _resendVisible.value = false
        _timerVisible.value = true
        startTimer() // Restart the timer when resend is clicked
    }

    // Method to start the countdown timer
    private fun startTimer() {
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                _timerText.value = String.format("%02d:%02d", minutes, secs) + "s" // Update timer text
            }

            override fun onFinish() {
                _timerText.value = "00:00" // Timer finished
                _resendVisible.value = true // Show resend button
                _timerVisible.value = false // Hide timer
            }
        }.start()
    }

    // Clean up resources when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer to avoid memory leaks
    }
}