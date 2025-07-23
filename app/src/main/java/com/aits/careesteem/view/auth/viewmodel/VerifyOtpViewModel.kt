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
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.CreateHashToken
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.google.gson.Gson
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
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    //var userData:SendOtpUserLoginResponse.Data? = null
    var userMobile: String? = null
    var userCountryId: String? = null

    // OtpVerifyResponse
    private val _otpVerifyResponse = MutableLiveData<OtpVerifyResponse?>()
    val otpVerifyResponse: LiveData<OtpVerifyResponse?> get() = _otpVerifyResponse

    // LiveData for OTP input and error message
    val otp = MutableLiveData<String>()
    val otpError = MutableLiveData<String?>()

    // MutableLiveData for checkbox state
    private val _onTermsCheck = MutableLiveData<Boolean>()
    val onTermsCheck: LiveData<Boolean> get() = _onTermsCheck

    private val _isVerifyButtonEnabled = MutableLiveData<Boolean>()
    val isVerifyButtonEnabled: LiveData<Boolean> get() = _isVerifyButtonEnabled

    init {
        _onTermsCheck.value = false // Default state (unchecked)
        _isVerifyButtonEnabled.value = false
        startTimer()

        otp.observeForever {
            updateVerifyButtonState()
        }

        _onTermsCheck.observeForever {
            updateVerifyButtonState()
        }
    }

    private fun updateVerifyButtonState() {
        val otpValue = otp.value
        val isOtpValid =
            otpValue != null && otpValue.length == 6 && otpValue.matches(Regex("^[0-9]{6}$"))
        val isCheckboxChecked = _onTermsCheck.value == true
        _isVerifyButtonEnabled.value = isOtpValid && isCheckboxChecked
    }

    // Function to update checkbox state
    fun onCheckboxChecked(isChecked: Boolean) {
        _onTermsCheck.value = isChecked
    }

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
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.sendOtpUserLogin(
                    contactNumber = userMobile!!,
                    telephoneCodes = userCountryId!!
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        AlertUtils.showToast(
                            activity,
                            apiResponse.message ?: "OTP sent successfully",
                            ToastyType.SUCCESS
                        )
                        // check token null or not
//                        editor.putString(SharedPrefConstant.HASH_TOKEN, apiResponse.data.token.toString())
//                        editor.apply()
                    }
                    _resendVisible.value = false
                    _timerVisible.value = true
                    startTimer() // Restart the timer when resend is clicked
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(
                    activity,
                    "Request Timeout. Please try again.",
                    ToastyType.ERROR
                )
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            } catch (e: Exception) {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
                _timerText.value =
                    String.format("%02d:%02d", minutes, secs) + "s" // Update timer text
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
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.verifyOtp(
                    contactNumber = userMobile!!,
                    countryCode = userCountryId!!,
                    otp = otp.value!!.toInt(),
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    fcmToken = sharedPreferences.getString(SharedPrefConstant.FCM_TOKEN, null)
                        .toString(),
                )

                if (response.isSuccessful) {
                    _isLoading.value = false
                    response.body()?.let { apiResponse ->
                        _otpVerifyResponse.value = apiResponse
                        AlertUtils.showToast(
                            activity,
                            apiResponse.message ?: "OTP verified successfully",
                            ToastyType.SUCCESS
                        )
                        editor.putString(SharedPrefConstant.CONTACT_NUMBER, userCountryId)
                        editor.putString(SharedPrefConstant.TELEPHONE_CODE, userCountryId)
                        //editor.putString(SharedPrefConstant.HASH_TOKEN, apiResponse.data[0].hash_token.toString())
                        editor.apply()
                    }
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(
                    activity,
                    "Request Timeout. Please try again.",
                    ToastyType.ERROR
                )
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            } catch (e: Exception) {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // CreateHashToken
    private val _createHashToken = MutableLiveData<CreateHashToken?>()
    val createHashToken: LiveData<CreateHashToken?> get() = _createHashToken

    fun onAgencySelected(activity: Activity, dbData: OtpVerifyResponse.DbList) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.selectDbName(
                    contactNumber = dbData.contact_number,
                    userId = dbData.id,
                    agencyId = dbData.agency_id
                )

                if (response.isSuccessful) {
                    _isLoading.value = false
                    response.body()?.let { apiResponse ->
                        //AlertUtils.showToast(activity, "Agency Selected Successfully")
                        val gson = Gson()
                        val dataString = gson.toJson(apiResponse.data[0])
                        editor.putString(SharedPrefConstant.USER_DATA, dataString)
                        editor.putString(
                            SharedPrefConstant.HASH_TOKEN,
                            apiResponse.data[0].hash_token.toString()
                        )
                        editor.apply()
                        _createHashToken.value = apiResponse
                    }
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(
                    activity,
                    "Request Timeout. Please try again.",
                    ToastyType.ERROR
                )
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            } catch (e: Exception) {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}