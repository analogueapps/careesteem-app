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
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
    private val dbRepository: VisitRepository,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
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
        _prnMedicationList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {

                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    val localMedications = dbRepository.getMedicationListByVisitsDetailsId(visitDetailsId)
                    val afterList = localMedications.map { med ->
                        MedicationDetailsListResponse.Data(
                            blister_pack_created_by = med.blister_pack_created_by ?: "",// fill if available in DB
                            blister_pack_date = med.blister_pack_date ?: "",
                            blister_pack_details_id = med.blister_pack_details_id ?: "",
                            blister_pack_end_date = med.blister_pack_end_date ?: "",
                            blister_pack_id = med.blister_pack_id ?: "",
                            blister_pack_start_date = med.blister_pack_start_date ?: "",
                            blister_pack_user_id = med.blister_pack_user_id ?: "",
                            by_exact_date = med.by_exact_date ?:"",
                            by_exact_end_date = med.by_exact_end_date ?: "",
                            by_exact_start_date = med.by_exact_start_date ?: "",
                            client_id = med.client_id ?: "",
                            day_name = med.day_name ?: "",
                            medication_id = med.medication_id,
                            medication_route_name = med.medication_route_name ?: "",
                            medication_support = med.medication_support ?: "",
                            medication_type = med.medication_type ?: "",
                            nhs_medicine_name = med.nhs_medicine_name ?: "",
                            quantity_each_dose = med.quantity_each_dose ?: "",
                            scheduled_created_by = med.scheduled_created_by ?: "",
                            scheduled_date = med.scheduled_date ?: "",
                            scheduled_details_id = med.scheduled_details_id ?: "",
                            prn_details_id = med.prn_details_id ?: "",
                            scheduled_end_date = med.scheduled_end_date ?: "",
                            scheduled_id = med.scheduled_id ?: "",
                            scheduled_start_date = med.scheduled_start_date ?: "",
                            scheduled_user_id = med.scheduled_user_id ?: "",
                            select_preference = med.select_preference ?: "",
                            by_exact_time = med.medication_time ?: "",
                            session_type = med.session_type ?: "",
                            visit_details_id = med.visitDetailsId ?: "",
                            prn_id = med.prn_id ?: "",
                            prn_start_date = med.prn_start_date ?: "",
                            prn_end_date = med.prn_end_date ?: "",
                            dose_per = med.dose_per ?: 0,
                            doses = med.doses ?: 0,
                            time_frame = med.time_frame ?: "",
                            prn_offered = med.prn_offered ?: "",
                            prn_be_given = med.prn_be_given ?: "",
                            prn_user_id = med.prn_user_id ?: "",
                            status = med.status ?: "",
                            prn_details_status = med.prn_details_status ?: "",
                            prn_created_by = med.prn_created_by ?: "",
                            body_map_image_url = med.bodyMapImageUrl?.let {
                                Gson().fromJson(it, Array<String>::class.java).toList()
                            } ?: emptyList(),
                            body_part_names = med.bodyPartNames?.let {
                                Gson().fromJson(it, Array<String>::class.java).toList()
                            } ?: emptyList(),
                            carer_notes = med.carer_notes ?: "",
                            additional_instructions = med.additional_instructions ?: "" // map if you store this locally
                        )
                    }
                    updateList(afterList, visitDetailsId)
                    return@launch
                }

                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.getMedicationDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        //_medicationList.value = list.data
                        updateList(list.data, visitDetailsId)
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

    private fun updateList(list: List<MedicationDetailsListResponse.Data>, visitDetailsId: String){
        val normalList = list.filter {
            it.medication_type.equals("Blister Pack", ignoreCase = true) ||
                    it.medication_type.equals("Scheduled", ignoreCase = true) ||
                    (it.medication_type.equals(
                        "PRN",
                        ignoreCase = true
                    ) && it.visit_details_id == visitDetailsId)
        }
        val prnList = list.filter {
            it.medication_type.equals(
                "PRN",
                ignoreCase = true
            ) && it.visit_details_id != visitDetailsId
        }
        _completeCount.value = list.count {
//                            (it.medication_type.equals("Blister Pack", ignoreCase = true) ||
//                                    it.medication_type.equals("Scheduled", ignoreCase = true)) &&
//                                    it.status != "Scheduled" && it.status != "Not Scheduled"
            it.status != "Scheduled" && it.status != "Not Scheduled"
        }
        _medicationList.value = normalList
        _prnMedicationList.value = prnList
    }

    fun medicationBlisterPack(
        activity: Activity,
        clientId: String,
        visitDetailsId: String,
        blisterPackDetailsId: String,
        status: String,
        carerNotes: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    dbRepository.updateMedicationByBlisterPackDetailsId(
                        blisterPackDetailsId = blisterPackDetailsId,
                        status = status,
                        carerNotes = carerNotes,
                        medicationSync = true,
                        medicationBlisterPack = true
                    )
                    AlertUtils.showToast(
                        activity,
                        "Medication updated successfully",
                        ToastyType.SUCCESS
                    )
                    return@launch
                }

                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.medicationBpDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    blisterPackDetailsId = blisterPackDetailsId,
                    status = status,
                    carerNotes = carerNotes
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
                if (status != "Fully Taken") {
                    automaticAlerts(
                        activity = activity,
                        status = status,
                        scheduledId = "",
                        blisterPackId = blisterPackDetailsId,
                        visitDetailsId = visitDetailsId,
                        clientId = clientId
                    )
                }
            }
        }
    }

    private fun automaticAlerts(
        activity: Activity,
        status: String,
        scheduledId: Any,
        blisterPackId: Any,
        visitDetailsId: String,
        clientId: String,
    ) {
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                    val autoAlertEntity = AutoAlertEntity(
                        colId = System.currentTimeMillis().toString(),
                        visitDetailsId = visitDetailsId,
                        clientId = clientId,
                        uatId = null,
                        userId = null,
                        alertType = "Medication $status",
                        alertStatus = "Action Required",
                        createdAt = DateTimeUtils.getCurrentTimestampGMT(),
                        alertAction = 3,
                        todoDetailsId = null,
                        scheduledId = scheduledId.toString(),
                        blisterPackId = blisterPackId.toString(),
                        alertSync = true,
                    )
                    dbRepository.insertAutoAlerts(autoAlertEntity = autoAlertEntity)
                    return@launch
                }

                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.automaticMedicationAlerts(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    scheduledId = scheduledId,
                    blisterPackId = blisterPackId,
                    visitDetailsId = visitDetailsId,
                    clientId = clientId,
                    alertType = "Medication $status",
                    alertStatus = "Action Required",
                    createdAt = DateTimeUtils.getCurrentTimestampGMT()
                )

                if (response.isSuccessful) {
                    //_isCheckOutEligible.value = true
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showLog("activity", "Request Timeout. Please try again.")
            } catch (e: HttpException) {
                AlertUtils.showLog("activity", "Server error: ${e.message}")
            } catch (e: Exception) {
                AlertUtils.showLog("activity", "An error occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun medicationScheduled(
        activity: Activity,
        clientId: String,
        visitDetailsId: String,
        scheduledDetailsId: String,
        status: String,
        carerNotes: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    dbRepository.updateMedicationByScheduledDetailsId(
                        scheduledDetailsId = scheduledDetailsId,
                        status = status,
                        carerNotes = carerNotes,
                        medicationSync = true,
                        medicationScheduled = true
                    )
                    AlertUtils.showToast(
                        activity,
                        "Medication updated successfully",
                        ToastyType.SUCCESS
                    )
                    return@launch
                }

                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.medicationScheduledDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    scheduledDetailsId = scheduledDetailsId,
                    status = status,
                    carerNotes = carerNotes
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
                if (status != "Fully Taken") {
                    automaticAlerts(
                        activity = activity,
                        status = status,
                        scheduledId = scheduledDetailsId,
                        blisterPackId = "",
                        visitDetailsId = visitDetailsId,
                        clientId = clientId
                    )
                }
            }
        }
    }

    fun medicationPrn(
        activity: Activity,
        visitDetailsId: String,
        medicationDetails: MedicationDetailsListResponse.Data,
        status: String,
        carerNotes: String
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

                val response = repository.medicationPrnDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = medicationDetails.client_id,
                    medicationId = medicationDetails.medication_id,
                    prnId = medicationDetails.prn_id,
                    doesPer = medicationDetails.dose_per,
                    doses = medicationDetails.doses,
                    timeFrame = medicationDetails.time_frame,
                    prnOffered = medicationDetails.prn_offered,
                    prnBeGiven = medicationDetails.prn_be_given,
                    visitDetailsId = visitDetailsId,
                    userId = userData.id,
                    medicationTime = "",
                    createdAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT(),
                    carerNotes = carerNotes,
                    status = status
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

    fun medicationPrnUpdate(
        activity: Activity,
        visitDetailsId: String,
        prnDetailsId: String,
        status: String,
        carerNotes: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    dbRepository.updateMedicationByPrnDetailsId(
                        prnDetailsId = prnDetailsId,
                        status = status,
                        carerNotes = carerNotes,
                        medicationSync = true,
                        medicationPrnUpdate = true
                    )
                    AlertUtils.showToast(
                        activity,
                        "Medication updated successfully",
                        ToastyType.SUCCESS
                    )
                    return@launch
                }

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

                val response = repository.updateMedicationPrn(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    prnDetailsId = prnDetailsId,
                    status = status,
                    carerNotes = carerNotes
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
                getMedicationDetails(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

}