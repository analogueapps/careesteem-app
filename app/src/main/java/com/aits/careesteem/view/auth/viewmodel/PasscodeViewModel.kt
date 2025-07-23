/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
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
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class PasscodeViewModel @Inject constructor(
    private val repository: Repository,
    private val errorHandler: ErrorHandler,
    private val sharedPreferences: SharedPreferences,
    private val editor: Editor,
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    var userData: CreateHashToken.Data? = null

    val isPasscodeVerified = MutableLiveData<Boolean>()

    // CreatePasscodeResponse
    private val _createPasscodeResponse = MutableLiveData<Boolean?>()
    val createPasscodeResponse: LiveData<Boolean?> get() = _createPasscodeResponse

    fun createPasscode(activity: Activity, passcode: String) {
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

                val response = repository.createPasscode(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    contactNumber = userData?.contact_number!!,
                    passcode = passcode.toInt()
                )

//                if (response.isSuccessful) {
//                    response.body()?.let { apiResponse ->
//                        _createPasscodeResponse.value = true
//                        AlertUtils.showToast(activity, apiResponse.message ?: "Created passcode successfully")
//                        editor.apply()
//                    }
//                } else {
//                    errorHandler.handleErrorResponse(response, activity)
//                }
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(
                        activity,
                        jsonObject.optString("message"),
                        ToastyType.SUCCESS
                    )
                    _createPasscodeResponse.value = true
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                    _createPasscodeResponse.value = false
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

    fun loginViaPasscode(activity: Activity, passcode: String) {
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

//                AlertUtils.showLog("passwordToCheck",""+passcode)
//                AlertUtils.showLog("hashedPassword",""+sharedPreferences.getString(SharedPrefConstant.LOGIN_PASSCODE, null))
//
//                // Verify the password
//                val isMatch = BCrypt.checkpw(passcode, sharedPreferences.getString(SharedPrefConstant.LOGIN_PASSCODE, null))
//
//                if (isMatch) {
//                    isPasscodeVerified.value = true
//                } else {
//                    isPasscodeVerified.value = false
//                }

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.verifyPasscode(
                    contactNumber = userData?.contact_number!!,
                    passcode = passcode.toInt(),
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(
                        activity,
                        jsonObject.optString("message"),
                        ToastyType.SUCCESS
                    )
                    isPasscodeVerified.value = true
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                    isPasscodeVerified.value = false
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