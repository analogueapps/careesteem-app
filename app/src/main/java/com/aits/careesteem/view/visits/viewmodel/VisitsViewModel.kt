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
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.view.VisitsFragmentDirections
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class VisitsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
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

    private val _visitCreated = MutableSharedFlow<Boolean>(replay = 0)
    val visitCreated = _visitCreated.asSharedFlow()

    @SuppressLint("NewApi")
    fun getVisits(activity: Activity, visitDate: String) {
        _visitsList.value = emptyList()
        _scheduledVisits.value = emptyList()
        _inProgressVisits.value = emptyList()
        _completedVisits.value = emptyList()
        _notCompletedVisits.value = emptyList()
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

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.getVisitList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                    visitDate = visitDate
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitsList.value = list.data

                        val notCompleted = list.data.filter { visit ->
                            val startEmpty = visit.actualStartTime.getOrNull(0).isNullOrEmpty()
                            val endEmpty = visit.actualEndTime.getOrNull(0).isNullOrEmpty()

                            if (startEmpty && endEmpty) {
                                try {
                                    val planned =
                                        LocalDateTime.parse("${visit.visitDate}T${visit.plannedStartTime}")
                                    val now = LocalDateTime.now()
                                    Duration.between(planned, now).toHours() >= 4
                                } catch (e: Exception) {
                                    false
                                }
                            } else false
                        }.sortedBy { it.plannedStartTime }

                        // Scheduled = visits that are not in notCompleted, but still have empty actual start and end time
                        val scheduled = list.data
                            .filter {
                                it.actualStartTime.getOrNull(0).isNullOrEmpty() &&
                                        it.actualEndTime.getOrNull(0).isNullOrEmpty() &&
                                        !notCompleted.contains(it)
                            }
                            .sortedBy { it.plannedStartTime }

                        val inProgress = list.data.filter {
                            it.actualStartTime.getOrNull(0)?.isNotEmpty() == true &&
                                    it.actualEndTime.getOrNull(0).isNullOrEmpty()
                        }

                        val completed = list.data
                            .filter {
                                it.actualStartTime.getOrNull(0)?.isNotEmpty() == true &&
                                        it.actualEndTime.getOrNull(0)?.isNotEmpty() == true
                            }
                            .sortedBy { it.actualStartTime[0] }

                        // Set values
                        _scheduledVisits.value = scheduled
                        _inProgressVisits.value = inProgress
                        _completedVisits.value = completed
                        _notCompletedVisits.value = notCompleted
                    }
                } else {
                    if (response.code() == 404) {
                        return@launch
                    }
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

    fun checkInEligible(
        activity: Activity,
        visitDetails: VisitListResponse.Data?,
        action: Int,
        findNavController: NavController
    ) {
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

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)


                val response = repository.checkInEligible(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                )

                if (response.isSuccessful) {
                    if(action == 0) {
                        val action =
                            VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(
                                visitDetails!!.visitDetailsId,
                                0
                            )
                        findNavController.navigate(action)
                    } else {
                        _visitCreated.emit(true)
                    }
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                    when (response.code()) {
                        409 -> {
                            if(action == 0) {
                                AlertUtils.showToast(
                                    activity,
                                    "Active check-in already exists",
                                    ToastyType.WARNING
                                )
                            } else {
                                _visitCreated.emit(false)
                            }
                        }

                        else -> errorHandler.handleErrorResponse(response, activity)
                    }
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

    val _isCheckOutEligible = MutableLiveData<Boolean>()
    val isCheckOutEligible: LiveData<Boolean> get() = _isCheckOutEligible

    fun checkOutEligible(
        activity: Activity,
        visitDetails: VisitListResponse.Data,
        findNavController: NavController
    ) {
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

                val response = repository.checkOutEligible(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetails.visitDetailsId
                )

                if (response.isSuccessful) {
                    _isCheckOutEligible.value = true
                    if (AppConstant.isMoreThanTwoMinutesPassed(
                            visitDetails.visitDate.toString(),
                            visitDetails.actualStartTime!![0].toString()
                        )
                    ) {
                        val direction =
                            VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(
                                visitDetailsId = visitDetails.visitDetailsId,
                                action = 1
                            )
                        findNavController.navigate(direction)
                    } else {
                        //showToast("Checkout is only allowed after 2 minutes from check-in.")
                        AlertUtils.showToast(
                            activity,
                            "Checkout is only allowed after 2 minutes from check-in.",
                            ToastyType.WARNING
                        )
                    }
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                    when (response.code()) {
                        404 -> {
                            AlertUtils.showToast(
                                activity,
                                "Please complete all essential tasks before checkout",
                                ToastyType.WARNING
                            )
                        }

                        else -> errorHandler.handleErrorResponse(response, activity)
                    }
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