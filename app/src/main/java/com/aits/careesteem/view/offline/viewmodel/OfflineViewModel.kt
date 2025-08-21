package com.aits.careesteem.view.offline.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.NetworkManager
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant.generate24CharHexId
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.model.VisitLinkResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class OfflineViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
    private val networkManager: NetworkManager,
    private val dbRepository: VisitRepository
) : ViewModel() {

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> = _isCompleted

    fun callOfflineApiAlso(activity: Activity, visitDate: String) {
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

    // For Local database
    suspend fun saveVisitData(response: VisitLinkResponse) {
        withContext(Dispatchers.IO) {
            val total = response.data.size
            var current = 0
            dbRepository.clearAllTables()
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
                    uatId = visit.uatId.ifEmpty { generate24CharHexId() },
                    qrcode_token = visit.qrcode_token,
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

                // Update progress
                current++
                val percent = (current * 100) / total
                _progress.postValue(percent)
            }

            _isCompleted.postValue(true)
        }
    }
}