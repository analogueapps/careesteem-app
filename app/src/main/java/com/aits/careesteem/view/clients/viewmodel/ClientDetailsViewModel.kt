/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
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
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
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
class ClientDetailsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _aboutClient = MutableLiveData<ClientDetailsResponse.Data.AboutData>()
    val aboutClient: LiveData<ClientDetailsResponse.Data.AboutData> get() = _aboutClient

    private val _clientMyCareNetwork = MutableLiveData<List<ClientDetailsResponse.Data.MyCareNetworkData>>()
    val clientMyCareNetwork: LiveData<List<ClientDetailsResponse.Data.MyCareNetworkData>> get() = _clientMyCareNetwork

    // Care plan assessment
    private val _activityAssessmentData = MutableLiveData<ClientCarePlanAssessment.Data.ActivityAssessmentData>()
    val activityAssessmentData: LiveData<ClientCarePlanAssessment.Data.ActivityAssessmentData> get() = _activityAssessmentData

    // Risk Assessment List
    private val _activityRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>>()
    val activityRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>> get() = _activityRiskAssessmentData

    private val _behaviourRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>>()
    val behaviourRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>> get() = _behaviourRiskAssessmentData

    private val _cOSHHRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>>()
    val cOSHHRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>> get() = _cOSHHRiskAssessmentData

    private val _equipmentRegisterData = MutableLiveData<List<CarePlanRiskAssList.Data.EquipmentRegisterData>>()
    val equipmentRegisterData: LiveData<List<CarePlanRiskAssList.Data.EquipmentRegisterData>> get() = _equipmentRegisterData

    private val _financialRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>>()
    val financialRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>> get() = _financialRiskAssessmentData

    private val _medicationRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>>()
    val medicationRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>> get() = _medicationRiskAssessmentData

    private val _selfAdministrationRiskAssessmentData = MutableLiveData<List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>>()
    val selfAdministrationRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>> get() = _selfAdministrationRiskAssessmentData


    // Add uv data
    private val _userActualTimeData = MutableLiveData<AddUvVisitResponse.UserActualTimeData>()
    val userActualTimeData: LiveData<AddUvVisitResponse.UserActualTimeData> get() = _userActualTimeData

    fun getClientDetails(activity: Activity, clientId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.getClientDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    clientId = clientId
                )

                if (response.isSuccessful) {
                    
                    response.body()?.let { list ->
                        _clientMyCareNetwork.value = list.data.MyCareNetwork
                        _aboutClient.value = list.data.About
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

    fun getClientCarePlanAss(activity: Activity, clientId: Int) {
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.getClientCarePlanAss(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    //clientId = clientId
                    //clientId = 176
                    clientId = 169
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _activityAssessmentData.value = list.data[0].ActivityAssessment
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

            }
        }
    }

    fun getClientCarePlanRiskAss(activity: Activity, clientId: Int) {
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.getClientCarePlanRiskAss(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    //clientId = clientId
                    //clientId = 176
                    clientId = 169
                )

                if (response.isSuccessful) {
                    
                    response.body()?.let { list ->
                        _activityRiskAssessmentData.value = list.data[0].ActivityRiskAssessment
                        _behaviourRiskAssessmentData.value = list.data[0].BehaviourRiskAssessment
                        _cOSHHRiskAssessmentData.value = list.data[0].COSHHRiskAssessment
                        _equipmentRegisterData.value = list.data[0].EquipmentRegister
                        _financialRiskAssessmentData.value = list.data[0].FinancialRiskAssessment
                        _medicationRiskAssessmentData.value = list.data[0].MedicationRiskAssessment
                        _selfAdministrationRiskAssessmentData.value = list.data[0].SelfAdministrationRiskAssessment
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