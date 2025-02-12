/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val repository: Repository,
    private val errorHandler: ErrorHandler,
    private val sharedPreferences: SharedPreferences,
    private val editor: Editor,
): ViewModel(){

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    var userData:SendOtpUserLoginResponse.Data? = null
    var action:Int? = 1

    // OtpVerifyResponse
    private val _otpVerifyResponse = MutableLiveData<OtpVerifyResponse?>()
    val otpVerifyResponse: LiveData<OtpVerifyResponse?> get() = _otpVerifyResponse

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
    fun onResendOtp(activity: Activity) {
        if (action == 1) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    // Check if network is available before making the request
                    if (!NetworkUtils.isNetworkAvailable(activity)) {
                        AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                        return@launch
                    }

                    val response = repository.sendOtpUserLogin(
                        contactNumber = userData?.contact_number!!,
                        telephoneCodes = 96
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            AlertUtils.showToast(activity, apiResponse.message ?: "OTP sent successfully")
                            editor.putString(SharedPrefConstant.HASH_TOKEN, apiResponse.data.token.toString())
                            editor.apply()
                        }
                        _resendVisible.value = false
                        _timerVisible.value = true
                        startTimer() // Restart the timer when resend is clicked
                    } else {
                        errorHandler.handleErrorResponse(response, activity)
                    }
                } catch (e: SocketTimeoutException) {
                    AlertUtils.showToast(activity,"Request Timeout. Please try again.")
                } catch (e: HttpException) {
                    AlertUtils.showToast(activity, "Server error: ${e.message}")
                } catch (e: Exception) {
                    AlertUtils.showToast(activity,"An error occurred: ${e.message}")
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        } else if (action == 2) {
            _isLoading.value = true
            viewModelScope.launch {
                try {
                    // Check if network is available before making the request
                    if (!NetworkUtils.isNetworkAvailable(activity)) {
                        AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                        return@launch
                    }

                    val response = repository.forgotPasscode(
                        hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                        contactNumber = sharedPreferences.getString(SharedPrefConstant.CONTACT_NUMBER, null).toString(),
                        telephoneCodes = 96
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { apiResponse ->
                            AlertUtils.showToast(activity, apiResponse.message ?: "OTP sent successfully")
                        }
                        _resendVisible.value = false
                        _timerVisible.value = true
                        startTimer() // Restart the timer when resend is clicked
                    } else {
                        errorHandler.handleErrorResponse(response, activity)
                    }
                } catch (e: SocketTimeoutException) {
                    AlertUtils.showToast(activity,"Request Timeout. Please try again.")
                } catch (e: HttpException) {
                    AlertUtils.showToast(activity, "Server error: ${e.message}")
                } catch (e: Exception) {
                    AlertUtils.showToast(activity,"An error occurred: ${e.message}")
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
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

    fun callVerifyOtpApi(activity: Activity) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.verifyOtp(
                    contactNumber = userData?.contact_number!!,
                    otp = otp.value!!.toInt(),
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        _otpVerifyResponse.value = apiResponse
                        AlertUtils.showToast(activity, apiResponse.message ?: "OTP verified successfully")
                        editor.putString(SharedPrefConstant.CONTACT_NUMBER, userData?.contact_number)
                        editor.putString(SharedPrefConstant.HASH_TOKEN, apiResponse.data[0].hash_token.toString())
                        editor.apply()
                    }
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(activity,"Request Timeout. Please try again.")
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}")
            } catch (e: Exception) {
                AlertUtils.showToast(activity,"An error occurred: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}