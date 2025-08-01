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
import androidx.room.ColumnInfo
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_model.VisitEntity
import com.aits.careesteem.view.visits.db_model.VisitUpdateFields
import com.aits.careesteem.view.visits.db_model.toEntity
import com.aits.careesteem.view.visits.db_model.toVisit
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.view.VisitsFragmentDirections
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
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
    private val visitDao: VisitDao
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
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    loadFromDatabase(visitDate)
                    return@launch
                }

                val response = fetchFromApi(visitDate)

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        processApiResponse(list, visitDate)
                    } ?: loadFromDatabase(visitDate)
                } else {
                    handleApiError(response, activity, visitDate)
                }
            } catch (e: Exception) {
                handleException(e, activity, visitDate)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchFromApi(visitDate: String): Response<VisitListResponse> {
        val userData = getUserData()
        return repository.getVisitList(
            hashToken = getHashToken(),
            userId = userData.id,
            visitDate = visitDate
        )
    }

    private suspend fun processApiResponse(list: VisitListResponse, visitDate: String) {
        val apiVisits = list.data.map { it.toEntity() }
        val existingVisits = visitDao.getVisitsByDate(visitDate)
        val existingVisitsMap = existingVisits.associateBy { it.visitDetailsId }

        val (visitsToUpdate, visitsToInsert) = apiVisits.partition {
            existingVisitsMap.containsKey(it.visitDetailsId)
        }

        // Convert to update fields
        val updateFields = visitsToUpdate.map { visit ->
            VisitUpdateFields(
                id = visit.visitDetailsId,
                status = visit.visitStatus,
                startTime = visit.actualStartTime,
                endTime = visit.actualEndTime,
                TotalActualTimeDiff = visit.TotalActualTimeDiff
            )
        }

        // Execute in transaction
        withContext(Dispatchers.IO) {
            if (updateFields.isNotEmpty()) {
                visitDao.updateMultipleVisitFields(updateFields)
            }
            if (visitsToInsert.isNotEmpty()) {
                visitDao.insertAll(visitsToInsert)
            }
        }

        // Refresh data
        loadFromDatabase(visitDate)
        _visitCreated.emit(true)
    }

    private suspend fun loadFromDatabase(visitDate: String) {
        val visits = visitDao.getVisitsByDate(visitDate).map { it.toVisit() }
        updateVisitLists(visits)
    }

    private suspend fun handleApiError(
        response: Response<VisitListResponse>,
        activity: Activity,
        visitDate: String
    ) {
        if (response.code() == 404) {
            _visitCreated.emit(true)
        } else {
            loadFromDatabase(visitDate)
            errorHandler.handleErrorResponse(response, activity)
        }
    }

    private suspend fun handleException(e: Exception, activity: Activity, visitDate: String) {
        loadFromDatabase(visitDate)
        when (e) {
            is SocketTimeoutException -> {
                AlertUtils.showToast(activity, "Request Timeout. Please try again.", ToastyType.ERROR)
            }
            is HttpException -> {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            }
            else -> {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            }
        }
    }

    private fun getHashToken(): String {
        return sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString()
    }

    private suspend fun getUserData(): OtpVerifyResponse.Data {
        val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
        return Gson().fromJson(dataString, OtpVerifyResponse.Data::class.java)
    }

    private suspend fun apiCallFails(visitDate: String) {
        val visits = visitDao.getVisitsByDate(date = visitDate)
        updateVisitLists(visits.map { it.toVisit() })
    }

    @SuppressLint("NewApi")
    private fun updateVisitLists(visits: List<VisitListResponse.Data>) {
        _visitsList.value = visits

        // Not completed: No actual start or end time, and more than 4 hours late
        val notCompleted = visits.filter { visit ->
            val startEmpty = visit.actualStartTime.getOrNull(0).isNullOrEmpty()
            val endEmpty = visit.actualEndTime.getOrNull(0).isNullOrEmpty()

            if (startEmpty && endEmpty) {
                try {
                    val planned = LocalDateTime.parse("${visit.visitDate}T${visit.plannedStartTime}")
                    val now = LocalDateTime.now()
                    Duration.between(planned, now).toHours() >= 4
                } catch (e: Exception) {
                    false
                }
            } else false
        }.sortedBy { it.plannedStartTime }

        // Scheduled = visits that are not in notCompleted, but still have empty actual start and end time
        val scheduled = visits
            .filter {
                it.actualStartTime.getOrNull(0).isNullOrEmpty() &&
                        it.actualEndTime.getOrNull(0).isNullOrEmpty() &&
                        !notCompleted.contains(it)
            }
            .sortedBy { it.plannedStartTime }

        val inProgress = visits.filter {
            it.actualStartTime.getOrNull(0)?.isNotEmpty() == true &&
                    it.actualEndTime.getOrNull(0).isNullOrEmpty()
        }

        val completed = visits
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