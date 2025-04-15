/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.view.VisitsFragmentDirections
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class VisitsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _visitsList = MutableLiveData<List<VisitListResponse.Data>>()
    val visitsList: LiveData<List<VisitListResponse.Data>> get() = _visitsList

    private val _scheduledVisits = MutableLiveData<List<VisitListResponse.Data>>()
    val scheduledVisits: LiveData<List<VisitListResponse.Data>> get() = _scheduledVisits

    private val _inProgressVisits = MutableLiveData<List<VisitListResponse.Data>>()
    val inProgressVisits: LiveData<List<VisitListResponse.Data>> get() = _inProgressVisits

    private val _completedVisits = MutableLiveData<List<VisitListResponse.Data>>()
    val completedVisits: LiveData<List<VisitListResponse.Data>> get() = _completedVisits

    private val _notCompletedVisits = MutableLiveData<List<VisitListResponse.Data>>()
    val notCompletedVisits: LiveData<List<VisitListResponse.Data>> get() = _notCompletedVisits

    @SuppressLint("NewApi")
    fun getVisits(activity: Activity, visitDate: String) {
        _visitsList.value = emptyList()
        _scheduledVisits.value = emptyList()
        _inProgressVisits.value = emptyList()
        _completedVisits.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.getVisitList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    id = userData.id,
                    visitDate = visitDate
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitsList.value = list.data
//                        val scheduled = list.data.filter { it.visitStatus.equals("Scheduled", ignoreCase = true) || it.visitStatus.equals("Unscheduled", ignoreCase = true) }
//                        val inProgress = list.data.filter { it.visitStatus.equals("In Progress", ignoreCase = true) }
//                        val completed = list.data.filter { it.visitStatus.equals("Completed", ignoreCase = true) }

                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                        val scheduled = list.data.filter { it.actualStartTime[0].isEmpty() && it.actualEndTime[0].isEmpty() }
                        val inProgress = list.data.filter { it.actualStartTime[0].isNotEmpty() && it.actualEndTime[0].isEmpty() }
                        val completed = list.data.filter { it.actualStartTime[0].isNotEmpty() && it.actualEndTime[0].isNotEmpty() }
                        // New: notCompleted = scheduled but past 4 hours of planned start time
                        val notCompleted = list.data.filter {
                            val startTimeEmpty = it.actualStartTime.getOrNull(0).isNullOrEmpty()
                            val endTimeEmpty = it.actualEndTime.getOrNull(0).isNullOrEmpty()

                            if (startTimeEmpty && endTimeEmpty) {
                                try {
                                    val plannedDateTime = LocalDateTime.parse("${it.visitDate}T${it.plannedStartTime}")
                                    val now = LocalDateTime.now()
                                    val duration = Duration.between(plannedDateTime, now)
                                    return@filter duration.toHours() >= 4
                                } catch (e: Exception) {
                                    return@filter false // In case parsing fails
                                }
                            } else {
                                false
                            }
                        }

                        _scheduledVisits.value = scheduled
                        _inProgressVisits.value = inProgress
                        _completedVisits.value = completed
                        _notCompletedVisits.value = notCompleted
                    }
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
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

    val _isCheckOutEligible = MutableLiveData<Boolean>()
    val isCheckOutEligible: LiveData<Boolean> get() = _isCheckOutEligible

    fun checkOutEligible(activity: Activity, visitDetailsId: Int, findNavController: NavController) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.checkOutEligible(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    _isCheckOutEligible.value = true
                    val direction = VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(
                        visitDetailsId = visitDetailsId,
                        action = 1
                    )
                    findNavController.navigate(direction)
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                    when (response.code()) {
                        404 -> {
                            AlertUtils.showToast(activity, "Please complete all essential tasks before checkout")
                        }
                        else -> errorHandler.handleErrorResponse(response, activity)
                    }
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