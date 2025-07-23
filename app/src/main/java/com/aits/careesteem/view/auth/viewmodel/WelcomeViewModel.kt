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
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val repository: Repository,
    private val errorHandler: ErrorHandler,
    private val editor: Editor,
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    val phoneNumber = MutableLiveData<String>()
    val phoneNumberError = MutableLiveData<String?>()
    val countryCode = MutableLiveData<String>("ff84412b2bed11f091d77e1e")

    private val _isPhoneNumberValid = MutableLiveData<Boolean>(false)
    val isPhoneNumberValid: LiveData<Boolean> get() = _isPhoneNumberValid

    val isRequestOtpApiCall = MutableLiveData<Boolean>()

    // SendOtpUserLoginResponse
    private val _sendOtpUserLoginResponse = MutableLiveData<SendOtpUserLoginResponse?>()
    val sendOtpUserLoginResponse: LiveData<SendOtpUserLoginResponse?> get() = _sendOtpUserLoginResponse

    fun setCountryCode(newCode: String) {
        countryCode.value = newCode
        //validatePhoneNumber(phoneNumber.value ?: "", "")
    }

    // Method to update field
    fun setPhoneNumber(newPhone: CharSequence, start: Int, before: Int, count: Int) {
        phoneNumber.value = newPhone.toString()
        //phoneNumberError.value = validatePhoneNumber(newPhone.toString())
        validatePhoneNumber(newPhone.toString())
    }

    private fun validatePhoneNumber(number: String) {
        when {
            number.isBlank() -> {
                phoneNumberError.value = "Mobile number is required"
                _isPhoneNumberValid.value = false
            }

            !number.matches(Regex("^[0-9]+$")) -> {
                phoneNumberError.value = "Mobile number must contain only digits"
                _isPhoneNumberValid.value = false
            }

            else -> {
                phoneNumberError.value = null
                _isPhoneNumberValid.value = true
            }
        }
    }

    // Method to handle error field
    private fun validateUKPhoneNumber(phoneNumber: String): String? {
        return when {
            phoneNumber.isBlank() -> "Mobile number is required"
            //phoneNumber.length != 11 -> "Phone number must be 11 digits long"
            //!phoneNumber.startsWith("7") -> "Phone number must start with 7"
            !phoneNumber.matches(Regex("^[0-9]+\$")) -> "Mobile number must contain only digits"
            else -> null
        }
    }

    // Method to handle the button click
    fun onButtonClicked() {
        val allFieldsValid = !(phoneNumber.value.isNullOrBlank())

        if (allFieldsValid) {
            if (phoneNumberError.value == null) {
                isRequestOtpApiCall.value = true
            } else {
                isRequestOtpApiCall.value = false
            }
        } else {
            phoneNumberError.value = "Please enter a valid mobile number"
            isRequestOtpApiCall.value = false
        }
    }

    fun callRequestOtpApi(activity: Activity) {
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
                    contactNumber = phoneNumber.value!!,
                    telephoneCodes = countryCode.value!!
                    //telephoneCodes = "a13f4f5c2bed11f091d77e1e"
                )

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        _sendOtpUserLoginResponse.value = apiResponse
                        AlertUtils.showToast(
                            activity,
                            apiResponse.message ?: "OTP sent successfully",
                            ToastyType.SUCCESS
                        )
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