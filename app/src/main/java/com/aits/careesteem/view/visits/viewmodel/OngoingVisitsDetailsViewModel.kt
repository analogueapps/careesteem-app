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
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.model.AddVisitCheckInResponse
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.view.VisitsFragmentDirections
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class OngoingVisitsDetailsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
    private val dbRepository: VisitRepository,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isCheckOutEligible = MutableLiveData<Boolean>()
    val isCheckOutEligible: LiveData<Boolean> get() = _isCheckOutEligible

    private val _visitsDetails = MutableLiveData<VisitDetailsResponse.Data>()
    val visitsDetails: LiveData<VisitDetailsResponse.Data> get() = _visitsDetails

    private val _visitNotesList = MutableLiveData<List<ClientVisitNotesDetails.Data>>()
    val visitNotesList: LiveData<List<ClientVisitNotesDetails.Data>> get() = _visitNotesList

    fun getVisitDetails(activity: Activity, visitDetailsId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity)
                    //&& sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)
                    ) {


                    val localVisit = dbRepository.getVisitsDetailsById(visitDetailsId)

                    localVisit?.let { visit ->
//                        _visitsDetails.value = VisitDetailsResponse.Data(
//                            TotalActualTimeDiff = visit.TotalActualTimeDiff?.split(",") ?: emptyList(),
//                            actualEndTime = visit.actualEndTime?.split(",") ?: emptyList(),
//                            actualStartTime = visit.actualStartTime?.split(",") ?: emptyList(),
//                            chooseSessions = visit.chooseSessions ?: "",
//                            clientAddress = visit.clientAddress ?: "",
//                            clientCity = visit.clientCity ?: "",
//                            clientPostcode = visit.clientPostcode ?: "",
//                            clientId = visit.clientId ?: "",
//                            clientName = visit.clientName ?: "",
//                            bufferTime = visit.bufferTime ?: "",
//                            latitude = "", // fill from your DB if available
//                            longitude = "", // fill from your DB if available
//                            placeId = visit.placeId ?: "",
//                            plannedEndTime = visit.plannedEndTime ?: "",
//                            plannedStartTime = visit.plannedStartTime ?: "",
//                            profile_photo = emptyList(), // if stored, split here
//                            profile_photo_name = visit.profilePhotoName?.split(",") ?: emptyList(),
//                            client_profile_image_url = visit.clientProfileImageUrl ?: "",
//                            radius = visit.radius ?: 0,
//                            sessionTime = visit.sessionTime ?: "",
//                            sessionType = visit.sessionType ?: "",
//                            totalPlannedTime = visit.totalPlannedTime ?: "",
//                            uatId = visit.uatId?.toIntOrNull() ?: 0,
//                            userId = visit.userId?.split(",") ?: emptyList(),
//                            userName = visit.userName?.split(",") ?: emptyList(),
//                            usersRequired = visit.usersRequired ?: 0,
//                            visitDate = visit.visitDate ?: "",
//                            visitDetailsId = visit.visitDetailsId,
//                            visitStatus = visit.visitStatus ?: "",
//                            visitType = visit.visitType ?: "",
//                        )
                        _visitsDetails.value = VisitDetailsResponse.Data(
                            TotalActualTimeDiff = toStringList(visit.TotalActualTimeDiff),
                            actualEndTime = toStringList(visit.actualEndTime),
                            actualStartTime = toStringList(visit.actualStartTime),
                            chooseSessions = visit.chooseSessions ?: "",
                            clientAddress = visit.clientAddress ?: "",
                            clientCity = visit.clientCity ?: "",
                            clientPostcode = visit.clientPostcode ?: "",
                            clientId = visit.clientId ?: "",
                            clientName = visit.clientName ?: "",
                            bufferTime = visit.bufferTime ?: "",
                            latitude = "",
                            longitude = "",
                            placeId = visit.placeId ?: "",
                            plannedEndTime = visit.plannedEndTime ?: "",
                            plannedStartTime = visit.plannedStartTime ?: "",
                            profile_photo = emptyList(),
                            profile_photo_name = toStringList(visit.profilePhotoName),
                            client_profile_image_url = visit.clientProfileImageUrl ?: "",
                            radius = visit.radius ?: 0,
                            sessionTime = visit.sessionTime ?: "",
                            sessionType = visit.sessionType ?: "",
                            totalPlannedTime = visit.totalPlannedTime ?: "",
                            uatId = visit.uatId?.toIntOrNull() ?: 0,
                            userId = toStringList(visit.userId),
                            userName = toStringList(visit.userName),
                            usersRequired = visit.usersRequired ?: 0,
                            visitDate = visit.visitDate ?: "",
                            visitDetailsId = visit.visitDetailsId,
                            visitStatus = visit.visitStatus ?: "",
                            visitType = visit.visitType ?: "",
                        )
                    }
                    return@launch
                }


                // Check if network is available before making the request
//                if (!NetworkUtils.isNetworkAvailable(activity)) {
//                    AlertUtils.showToast(
//                        activity,
//                        "No Internet Connection. Please check your network and try again.",
//                        ToastyType.ERROR
//                    )
//                    return@launch
//                }

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.getVisitDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitsDetails.value = list.data[0]
                    }
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

    fun getPreviousVisitNotes(activity: Activity, visitDetailsId: String) {
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity)
                    //&& sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)
                    ) {
                    return@launch
                }

                // Check if network is available before making the request
//                if (!NetworkUtils.isNetworkAvailable(activity)) {
//                    AlertUtils.showToast(
//                        activity,
//                        "No Internet Connection. Please check your network and try again.",
//                        ToastyType.ERROR
//                    )
//                    return@launch
//                }

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.getClientPreviousVisitNotesDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _visitNotesList.value = list.data
                    }
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
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
                editor.putBoolean(SharedPrefConstant.SHOW_PREVIOUS_NOTES, false)
                editor.apply()
            }
        }
    }

    fun checkOutEligible(activity: Activity, visitDetailsId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity)
                    //&& sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)
                    ) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val todosList = dbRepository.getTodosWithEssentialAndEmptyOutcome(visitDetailsId)
                        val medsList = dbRepository.getMedicationsWithScheduled(visitDetailsId)

                        withContext(Dispatchers.Main) {
                            if (todosList.isNotEmpty() || medsList.isNotEmpty()) {
                                AlertUtils.showToast(
                                    activity,
                                    "Please complete all essential tasks before checkout",
                                    ToastyType.ERROR
                                )
                                return@withContext
                            }
                            _isCheckOutEligible.value = true
                        }
                    }
                    return@launch
                }


                // Check if network is available before making the request
//                if (!NetworkUtils.isNetworkAvailable(activity)) {
//                    AlertUtils.showToast(
//                        activity,
//                        "No Internet Connection. Please check your network and try again.",
//                        ToastyType.ERROR
//                    )
//                    return@launch
//                }

                val response = repository.checkOutEligible(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    _isCheckOutEligible.value = true
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                    when (response.code()) {
                        404 -> {
                            AlertUtils.showToast(
                                activity,
                                "Please complete all essential tasks before checkout",
                                ToastyType.ERROR
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

    private fun toStringList(value: Any?): List<String> {
        return when (value) {
            is List<*> -> value.filterIsInstance<String>() // already a list
            is String -> if (value.isNotBlank()) value.split(",") else emptyList()
            else -> emptyList()
        }
    }

}