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
import androidx.room.PrimaryKey
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant.generate24CharHexId
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.VisitNotesEntity
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class VisitNotesViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
    private val dbRepository: VisitRepository,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _visitNotesList = MutableLiveData<List<ClientVisitNotesDetails.Data>>()
    val visitNotesList: LiveData<List<ClientVisitNotesDetails.Data>> get() = _visitNotesList

    fun getVisitNotesList(activity: Activity, visitDetailsId: String) {
        _visitNotesList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(
                        SharedPrefConstant.WORK_ON_OFFLINE,
                        false
                    )
                ) {
                    val localData = dbRepository.getAllVisitNotesByVisitDetailsId(visitDetailsId = visitDetailsId)
                    _visitNotesList.value = localData.map { notes ->
                        ClientVisitNotesDetails.Data(
                            createdAt = notes.createdAt ?: "",
                            createdByUserId = notes.createdByUserid ?: "",
                            createdByUserName = notes.createdByUserName ?: "",
                            id = notes.visitNotesId, // visitNotesId maps to id in Data
                            updatedAt = notes.updatedAt ?: "",
                            updatedByUserId = notes.updatedByUserid ?: "",
                            updatedByUserName = notes.updatedByUserName ?: "",
                            visitDetaiId = notes.visitDetailsId ?: "", // note: your Data class has a typo: visitDetaiId instead of visitDetailId
                            visitNotes = notes.visitNotes ?: ""
                        )
                    }
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

                val response = repository.getClientVisitNotesDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitNotesList.value = list.data
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

    fun addVisitNotes(activity: Activity, visitDetailsId: String, visitNotes: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                if (!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(
                        SharedPrefConstant.WORK_ON_OFFLINE,
                        false
                    )
                ) {
                    val visitNotesEntity = VisitNotesEntity(
                        visitNotesId = generate24CharHexId(),
                        visitDetailsId = visitDetailsId,
                        visitNotes = visitNotes,
                        createdByUserid = userData.id,
                        createdByUserName = userData.first_name + " " + userData.last_name,
                        updatedByUserid = userData.id,
                        updatedByUserName = userData.first_name + " " + userData.last_name,
                        createdAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT(),
                        updatedAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT(),
                        notesSync = true
                    )
                    dbRepository.insertVisitNotes(visitNotesEntity)
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

                val response = repository.addClientVisitNotesDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId,
                    visitNotes = visitNotes,
                    createdByUserid = userData.id,
                    updatedByUserid = userData.id,
                    createdAt = DateTimeUtils.getCurrentTimestampAddVisitNotesGMT()
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
                getVisitNotesList(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

    fun updateVisitNotes(
        activity: Activity,
        visitDetailsId: String,
        createdByUserid: String,
        visitNotesId: String,
        visitNotes: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                if (!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(
                        SharedPrefConstant.WORK_ON_OFFLINE,
                        false
                    )
                ) {
                    dbRepository.updateVisitNotesById(
                        visitNotesId = visitNotesId,
                        visitNotes = visitNotes,
                        updatedByUserid = userData.id,
                        updatedByUserName = userData.first_name + " " + userData.last_name,
                        updatedAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
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

                val response = repository.updateVisitNotesDetail(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitNotesId = visitNotesId,
                    visitDetailsId = visitDetailsId,
                    visitNotes = visitNotes,
                    createdByUserid = createdByUserid.toString(),
                    updatedByUserid = userData.id,
                    updatedAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
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
                getVisitNotesList(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
            }
        }
    }

}