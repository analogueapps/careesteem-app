/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.alerts.viewmodel

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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
import com.aits.careesteem.view.alerts.model.ClientNameListResponse
import com.aits.careesteem.view.alerts.model.FileModel
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddAlertsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
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
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.getClientsList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    id = 506,
                    visitDate = "2025-02-03"
                )

                if (response.isSuccessful) {

                    response.body()?.let { list ->
                        _clientsList.value = list.data
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

    fun addAlerts(activity: Activity, clientId: Int, visitDetailsId: Int, severityOfConcern: String, concernDetails: String, fileList: MutableList<FileModel>) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val currentTime = Calendar.getInstance()
                val createdAtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault())
                val createdAt = createdAtFormat.format(currentTime.time)

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val bodyPartType = fileList.joinToString(", ") { it.bodyPartType }
                val bodyPartNames = fileList.joinToString(", ") { it.bodyPartNames }
                val fileName = fileList.joinToString(", ") { it.fileName }



                val response =  repository.sendAlert(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    clientId = clientId.toString(),
                    userId = userData.id.toString(),
                    visitDetailsId = visitDetailsId.toString(),
                    severityOfConcern = severityOfConcern,
                    concernDetails = concernDetails,
                    bodyPartType = bodyPartType,
                    bodyPartNames = bodyPartNames,
                    fileName = fileName,
                    createdAt = createdAt,
                    images = createFiles(fileList, activity)
                )

                if (response.isSuccessful) {
                    AlertUtils.showToast(activity, "Alert added successfully")
                    _alertAdded.value = true
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                    _alertAdded.value = false
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

    fun getFilterVisits(clientId: Int) {
        _filterVisitsList.value = _visitsList.value?.filter { it.clientId == clientId }.orEmpty()
    }

    fun getVisits(activity: Activity) {
        _visitsList.value = emptyList()
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
                    visitDate = "2025-02-03"
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitsList.value = list.data
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

    private fun createFiles(fileModels: List<FileModel>, context: Context): List<File> {
        return fileModels.mapNotNull { fileModel ->
            println(fileModel.filePath)
            val fileUri = Uri.fromFile(File(fileModel.filePath)) // Convert filePath to Uri
            AppConstant.uriToFile(context, fileUri) // Return the file directly
        }
    }


}