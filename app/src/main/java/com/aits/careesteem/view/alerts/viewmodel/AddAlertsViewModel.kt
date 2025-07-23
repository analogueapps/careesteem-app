/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.alerts.viewmodel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.model.ClientNameListResponse
import com.aits.careesteem.view.alerts.model.FileModel
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class AddAlertsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _clientsList = MutableLiveData<List<ClientNameListResponse.Data>>()
    val clientsList: LiveData<List<ClientNameListResponse.Data>> get() = _clientsList

    private val _visitsList = MutableLiveData<List<VisitListResponse.Data>>()
    val visitsList: LiveData<List<VisitListResponse.Data>> get() = _visitsList

    private val _filterVisitsList = MutableLiveData<List<VisitListResponse.Data>>()
    val filterVisitsList: LiveData<List<VisitListResponse.Data>> get() = _filterVisitsList

    private val _alertAdded = MutableLiveData<Boolean>().apply { value = false }
    val alertAdded: LiveData<Boolean> get() = _alertAdded

    fun getClientsList(activity: Activity) {
        _clientsList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val userData =
                    sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)?.let {
                        Gson().fromJson(it, OtpVerifyResponse.Data::class.java)
                    } ?: return@launch

                val response = repository.getClientsListAlerts(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .orEmpty(),
                    userId = userData.id,
                    visitDate = DateTimeUtils.getCurrentDateGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.data?.let { _clientsList.value = it }
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

    fun getVisits(activity: Activity) {
        _visitsList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val userData =
                    sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)?.let {
                        Gson().fromJson(it, OtpVerifyResponse.Data::class.java)
                    } ?: return@launch

                val response = repository.getVisitList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .orEmpty(),
                    userId = userData.id,
                    visitDate = DateTimeUtils.getCurrentDateGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.data?.let { _visitsList.value = it }
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

    fun getFilterVisits(clientId: String) {
        _filterVisitsList.value = _visitsList.value?.filter { it.clientId == clientId }.orEmpty()
    }

    fun addAlerts(
        activity: Activity,
        clientId: String,
        visitDetailsId: String,
        severityOfConcern: String,
        concernDetails: String,
        fileList: MutableList<FileModel>
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val userData =
                    sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)?.let {
                        Gson().fromJson(it, OtpVerifyResponse.Data::class.java)
                    } ?: return@launch

                val bodyPartType = fileList.joinToString(", ") { it.bodyPartType }
                val bodyPartNames = fileList.joinToString(", ") { it.bodyPartNames }
                val fileName = fileList.joinToString(", ") { it.fileName }

                val response = repository.sendAlert(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .orEmpty(),
                    clientId = clientId.toString(),
                    userId = userData.id.toString(),
                    visitDetailsId = visitDetailsId.toString(),
                    severityOfConcern = severityOfConcern,
                    concernDetails = concernDetails,
                    bodyPartType = bodyPartType,
                    bodyPartNames = bodyPartNames,
                    fileName = fileName,
                    createdAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT(),
                    images = createFiles(fileList, activity)
                )

                if (response.isSuccessful) {
                    AlertUtils.showToast(activity, "Alert added successfully", ToastyType.SUCCESS)
                    _alertAdded.value = true
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                    _alertAdded.value = false
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

    private fun createFiles(fileModels: List<FileModel>, context: Context): List<File> {
        return fileModels.mapNotNull { fileModel ->
            val file = File(fileModel.filePath)
            if (file.exists() && file.name.isNotBlank()) file else null
        }
    }
}