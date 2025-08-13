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
import com.aits.careesteem.network.NetworkManager
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.AppConstant.generate24CharHexId
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.model.VisitLinkResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.view.VisitsFragmentDirections
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val networkManager: NetworkManager,
    private val dbRepository: VisitRepository
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
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                    val localList = dbRepository.getAllVisitsByDate(visitDate = visitDate)
                    _visitsList.value = localList.map { visitEntity ->
                        VisitListResponse.Data(
                            clientId = visitEntity.clientId ?: "",
                            visitDetailsId = visitEntity.visitDetailsId,
                            uatId = visitEntity.uatId?.toIntOrNull() ?: 0,
                            clientAddress = visitEntity.clientAddress ?: "",
                            clientCity = visitEntity.clientCity ?: "",
                            clientPostcode = visitEntity.clientPostcode ?: "",
                            clientName = visitEntity.clientName ?: "",
                            plannedEndTime = visitEntity.plannedEndTime ?: "",
                            plannedStartTime = visitEntity.plannedStartTime ?: "",
                            totalPlannedTime = visitEntity.totalPlannedTime ?: "",
                            userId = visitEntity.userId?.split(",") ?: emptyList(),
                            usersRequired = visitEntity.usersRequired ?: 0,
                            latitude = "", // fill if stored
                            longitude = "", // fill if stored
                            radius = visitEntity.radius ?: 0,
                            placeId = visitEntity.placeId ?: "",
                            visitDate = visitEntity.visitDate ?: "",
                            visitStatus = visitEntity.visitStatus ?: "",
                            visitType = visitEntity.visitType ?: "",
                            actualStartTime = visitEntity.actualStartTime?.split(",") ?: emptyList(),
                            actualEndTime = visitEntity.actualEndTime?.split(",") ?: emptyList(),
                            TotalActualTimeDiff = visitEntity.TotalActualTimeDiff?.split(",") ?: emptyList(),
                            userName = visitEntity.userName?.split(",") ?: emptyList(),
                            profile_photo_name = visitEntity.profilePhotoName?.split(",") ?: emptyList(),
                            bufferTime = visitEntity.bufferTime ?: "",
                            sessionType = visitEntity.sessionType ?: "",
                            sessionTime = visitEntity.sessionTime ?: "",
                            chooseSessions = visitEntity.chooseSessions ?: ""
                        )
                    }
                    updateValues()
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

                val response = repository.getVisitList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                    visitDate = visitDate
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitsList.value = list.data
                        updateValues()
                        _visitCreated.emit(true)
                    }
                } else {
                    if (response.code() == 404) {
                        _visitCreated.emit(true)
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
                callOfflineApiAlso(activity, visitDate)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun updateValues() {
        val notCompleted = visitsList.value?.filter { visit ->
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
        }?.sortedBy { it.plannedStartTime }

        // Scheduled = visits that are not in notCompleted, but still have empty actual start and end time
        val scheduled = visitsList.value?.filter {
                it.actualStartTime.getOrNull(0).isNullOrEmpty() &&
                        it.actualEndTime.getOrNull(0).isNullOrEmpty() &&
                        !notCompleted?.contains(it)!!
            }?.sortedBy { it.plannedStartTime }

        val inProgress = visitsList.value?.filter {
            it.actualStartTime.getOrNull(0)?.isNotEmpty() == true &&
                    it.actualEndTime.getOrNull(0).isNullOrEmpty()
        }

        val completed = visitsList.value?.filter {
                it.actualStartTime.getOrNull(0)?.isNotEmpty() == true &&
                        it.actualEndTime.getOrNull(0)?.isNotEmpty() == true
            }?.sortedBy { it.actualStartTime[0] }

        // Set values
        _scheduledVisits.value = scheduled ?: emptyList()
        _inProgressVisits.value = inProgress ?: emptyList()
        _completedVisits.value = completed ?: emptyList()
        _notCompletedVisits.value = notCompleted ?: emptyList()
    }

    private fun callOfflineApiAlso(activity: Activity, visitDate: String) {
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

                val response = repository.getVisitLinkDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                    visitDate = visitDate
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        saveVisitData(list)
                    }
                } else {
                    if (response.code() == 401 || response.code() == 402) {
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
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    val todosList = dbRepository.getTodosWithEssentialAndEmptyOutcome(visitDetails.visitDetailsId)
                    val medsList = dbRepository.getMedicationsWithScheduled(visitDetails.visitDetailsId)

                    if (todosList.isNotEmpty() || medsList.isNotEmpty()) {
                        AlertUtils.showToast(
                            activity,
                            "Please complete all essential tasks before checkout",
                            ToastyType.ERROR
                        )
                        return@launch
                    }
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

    // For Local database
    suspend fun saveVisitData(response: VisitLinkResponse) {
        dbRepository.clearMedications()
        viewModelScope.launch(Dispatchers.IO) {
            response.data.forEach { visit ->
                val visitEntity = VisitEntity(
                    visitDetailsId = visit.visitDetailsId,
                    agencyId = visit.agencyId,
                    bufferTime = visit.bufferTime,
                    chooseSessions = visit.chooseSessions,
                    clientAddress = visit.clientAddress,
                    clientCity = visit.clientCity,
                    clientId = visit.clientId,
                    clientName = visit.clientName,
                    clientPostcode = visit.clientPostcode,
                    clientProfileImageUrl = visit.client_profile_image_url,
                    gioStatus = visit.gioStatus,
                    placeId = visit.placeId,
                    plannedEndTime = visit.plannedEndTime,
                    plannedStartTime = visit.plannedStartTime,
                    profilePhotoName = visit.profile_photo_name.joinToString(","),
                    radius = visit.radius,
                    sessionTime = visit.sessionTime,
                    sessionType = visit.sessionType,
                    totalPlannedTime = visit.totalPlannedTime,
                    usersRequired = visit.usersRequired,
                    visitDate = visit.visitDate,
                    visitStatus = visit.visitStatus,
                    visitType = visit.visitType,
                    userId = visit.userId.joinToString(","),
                    userName = visit.userName.joinToString(","),
                    actualStartTime = visit.actualStartTime.joinToString(","),
                    actualEndTime = visit.actualEndTime.joinToString(","),
                    TotalActualTimeDiff = visit.TotalActualTimeDiff.joinToString(","),
                    actualStartTimeString = null,
                    actualEndTimeString = null,
                    uatId = visit.uatId ?: generate24CharHexId(),
                )
                dbRepository.insertVisit(visitEntity)

                val medicationEntities = visit.medications.map {
                    val visitDetailsId = when (val id = it.visit_details_id) {
                        is String -> id
                        is List<*> -> id.joinToString(",") // Flatten list to string
                        else -> ""
                    }

                    val medication_time = when (val time = it.medication_time) {
                        is String -> time
                        is List<*> -> time.joinToString(",") { s -> s?.toString() ?: "" }
                        else -> null
                    }

                    MedicationEntity(
                        //uniqueId = "${it.medication_id}_${normalizeVisitId(visitDetailsId)}",
                        medication_id = it.medication_id,
                        visitDetailsId = visitDetailsId,
                        nhs_medicine_name = it.nhs_medicine_name,
                        medication_time = medication_time,
                        medication_type = it.medication_type,
                        bodyMapImageUrl = Gson().toJson(it.body_map_image_url),
                        bodyPartNames = Gson().toJson(it.body_part_names),
                        dose_per = it.dose_per,
                        doses = it.doses,
                        medication_route_name = it.medication_route_name,
                        medication_support = it.medication_support,
                        quantity_each_dose = it.quantity_each_dose,
                        prn_be_given = it.prn_be_given,
                        prn_offered = it.prn_offered,
                        prn_user_id = it.prn_user_id,
                        status = it.status,
                        carer_notes = it.carer_notes,
                        file_name = it.file_name,
                        prn_created_by = it.prn_created_by,

                        blister_pack_created_by = it.blister_pack_created_by,
                        blister_pack_date = it.blister_pack_date,
                        blister_pack_details_id = it.blister_pack_details_id,
                        blister_pack_end_date = it.blister_pack_end_date,
                        blister_pack_id = it.blister_pack_id,
                        blister_pack_start_date = it.blister_pack_start_date,
                        blister_pack_user_id = it.blister_pack_user_id,

                        by_exact_date = it.by_exact_date,
                        by_exact_end_date = it.by_exact_end_date,
                        by_exact_start_date = it.by_exact_start_date,
                        client_id = it.client_id,
                        day_name = it.day_name,

                        scheduled_created_by = it.scheduled_created_by,
                        scheduled_date = it.scheduled_date,
                        scheduled_details_id = it.scheduled_details_id,
                        prn_details_id = null,
                        scheduled_end_date = it.scheduled_end_date,
                        scheduled_id = it.scheduled_id,
                        scheduled_start_date = it.scheduled_start_date,
                        scheduled_user_id = it.scheduled_user_id,
                        select_preference = it.select_preference,
                        session_type = it.session_type,

                        prn_id = it.prn_id,
                        prn_start_date = it.prn_start_date,
                        prn_end_date = it.prn_end_date,
                        time_frame = it.time_frame,
                        prn_details_status = null,
                        additional_instructions = it.additional_instructions,

                        medicationSync = false,
                        medicationBlisterPack = false,
                        medicationScheduled = false,
                        medicationPrn = false,
                        medicationPrnUpdate = false,

                        createdAt = System.currentTimeMillis()
                    )
                }
                dbRepository.insertMedications(medicationEntities)

                val todoEntities = visit.todoList.map {
                    TodoEntity(
                        todoDetailsId = it.todoDetailsId,
                        visitDetailsId = it.visitDetailsId,
                        additionalNotes = it.additionalNotes,
                        carerNotes = it.carerNotes,
                        todoEssential = it.todoEssential,
                        todoName = it.todoName,
                        todoOutcome = it.todoOutcome
                    )
                }
                dbRepository.insertTodos(todoEntities)
            }
        }
    }

    fun normalizeVisitId(raw: String): String {
        return raw.split(",")
            .map { it.trim() }
            .sorted()
            .joinToString(",")
    }



}