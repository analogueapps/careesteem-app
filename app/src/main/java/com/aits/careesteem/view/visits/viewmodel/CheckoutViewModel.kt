/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.visits.model.AddVisitCheckInResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _addVisitCheckInResponse = MutableLiveData<List<AddVisitCheckInResponse.Data>>()
    val addVisitCheckInResponse: LiveData<List<AddVisitCheckInResponse.Data>> get() = _addVisitCheckInResponse

    // Add uv data
    private val _userActualTimeData = MutableLiveData<AddUvVisitResponse.UserActualTimeData>()
    val userActualTimeData: LiveData<AddUvVisitResponse.UserActualTimeData> get() = _userActualTimeData

    fun addVisitCheckIn(activity: Activity, clientId: Int, visitDetailsId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val currentTime = Calendar.getInstance()
                // Formatting visit_date as "yyyy-MM-dd"
                val visitDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val visitDate = visitDateFormat.format(currentTime.time)

                // Formatting actual_start_time as "HH:mm:ss"
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val actualStartTime = timeFormat.format(currentTime.time)

                // Formatting created_at as "yyyy-MM-dd'T'HH:mm:ss"
                val createdAtFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val createdAt = createdAtFormat.format(currentTime.time)

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.addVisitCheckIn(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    clientId = clientId,
                    visitDetailsId = visitDetailsId,
                    userId = userData.id,
                    status = "",
                    actualStartTime = "",
                    createdAt = createdAt
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _addVisitCheckInResponse.value = list.data
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

    fun createUnscheduledVisit(activity: Activity, clientId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val currentTime = Calendar.getInstance()
                // Formatting visit_date as "yyyy-MM-dd"
                val visitDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val visitDate = visitDateFormat.format(currentTime.time)

                // Formatting actual_start_time as "HH:mm:ss"
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val actualStartTime = timeFormat.format(currentTime.time)

                // Formatting created_at as "yyyy-MM-dd'T'HH:mm:ss"
                val createdAtFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val createdAt = createdAtFormat.format(currentTime.time)

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.addUnscheduledVisits(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    userId = userData.id.toString(),
                    clientId = clientId,
                    visitDate = visitDate,
                    actualStartTime = actualStartTime,
                    createdAt = createdAt
                )

                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _userActualTimeData.value = list.userActualTimeData[0]
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