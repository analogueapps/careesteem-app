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
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _medicationList = MutableLiveData<List<MedicationDetailsListResponse.Data>>()
    val medicationList: LiveData<List<MedicationDetailsListResponse.Data>> get() = _medicationList

    // PRN
    private val _prnMedicationList = MutableLiveData<List<MedicationDetailsListResponse.Data>>()
    val prnMedicationList: LiveData<List<MedicationDetailsListResponse.Data>> get() = _prnMedicationList

    private val _completeCount = MutableLiveData<Int>().apply { value = 0 }
    val completeCount: LiveData<Int> get() = _completeCount

    fun getMedicationDetails(activity: Activity, visitDetailsId: String) {
        _medicationList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.getMedicationDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    //taskId = taskId
                    //visitDetailsId = "2565"
                    visitDetailsId = "2399"
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        //_medicationList.value = list.data
                        val normalList = list.data.filter { it.medication_type.equals("Blister Pack", ignoreCase = true) || it.medication_type.equals("Scheduled", ignoreCase = true) }
                        val prnList = list.data.filter { it.medication_type.equals("PRN", ignoreCase = true) }
                        _medicationList.value = normalList
                        _prnMedicationList.value = prnList
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

    fun medicationBlisterPack(activity: Activity, visitDetailsId: String, blisterPackDetailsId: Int, status: String, carerNotes: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.medicationBpDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    blisterPackDetailsId = blisterPackDetailsId,
                    status = status,
                    carerNotes = carerNotes
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(activity, jsonObject.optString("message"))
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

    fun medicationScheduled(activity: Activity, visitDetailsId: String, scheduledDetailsId: Int, status: String, carerNotes: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.medicationScheduledDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    scheduledDetailsId = scheduledDetailsId,
                    status = status,
                    carerNotes = carerNotes
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(activity, jsonObject.optString("message"))
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

    fun medicationPrn(activity: Activity, visitDetailsId: String, medicationDetails: MedicationDetailsListResponse.Data, status: String, carerNotes: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val currentTime = Calendar.getInstance()
                // Formatting created_at as "yyyy-MM-dd'T'HH:mm:ss"
                val createdAtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                val createdAt = createdAtFormat.format(currentTime.time)

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.medicationPrnDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    clientId = medicationDetails.client_id,
                    medicationId = medicationDetails.medication_id,
                    prnId = medicationDetails.prn_id,
                    doesPer = medicationDetails.dose_per,
                    doses = medicationDetails.doses,
                    timeFrame = medicationDetails.time_frame,
                    prnOffered = medicationDetails.prn_offered,
                    prnBeGiven = medicationDetails.prn_be_given,
                    visitDetailsId = visitDetailsId.toInt(),
                    userId = userData.id,
                    medicationTime = "",
                    createdAt = createdAt,
                    carerNotes = carerNotes,
                    status = status
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(activity, jsonObject.optString("message"))
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

}