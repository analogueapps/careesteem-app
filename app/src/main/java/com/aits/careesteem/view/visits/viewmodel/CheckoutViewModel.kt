/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.NetworkManager
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.unscheduled_visits.model.UpdateVisitCheckoutResponse
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.model.AddVisitCheckInResponse
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
    private val networkManager: NetworkManager,
    private val dbRepository: VisitRepository,
) : AndroidViewModel(application) {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for UI
    private val _isAutoCheckIn = MutableLiveData<Boolean>(true)
    val isAutoCheckIn: LiveData<Boolean> get() = _isAutoCheckIn

    // LiveData for UI
    private val _qrVerified = MutableLiveData<Boolean>()
    val qrVerified: LiveData<Boolean> get() = _qrVerified

    private val _addVisitCheckInResponseStart =
        MutableLiveData<List<AddVisitCheckInResponse.Data>>()
    val addVisitCheckInResponseStart: LiveData<List<AddVisitCheckInResponse.Data>> get() = _addVisitCheckInResponseStart

    private val _addVisitCheckInResponse = MutableLiveData<List<AddVisitCheckInResponse.Data>>()
    val addVisitCheckInResponse: LiveData<List<AddVisitCheckInResponse.Data>> get() = _addVisitCheckInResponse

    private val _updateVisitCheckoutResponseStart =
        MutableLiveData<List<UpdateVisitCheckoutResponse.Data>>()
    val updateVisitCheckoutResponseStart: LiveData<List<UpdateVisitCheckoutResponse.Data>> get() = _updateVisitCheckoutResponseStart

    private val _updateVisitCheckoutResponse =
        MutableLiveData<List<UpdateVisitCheckoutResponse.Data>?>()
    val updateVisitCheckoutResponse: MutableLiveData<List<UpdateVisitCheckoutResponse.Data>?> get() = _updateVisitCheckoutResponse

    // Add uv data
    private val _userActualTimeData = MutableLiveData<AddUvVisitResponse.UserActualTimeData>()
    val userActualTimeData: LiveData<AddUvVisitResponse.UserActualTimeData> get() = _userActualTimeData

    private val _isCheckOutEligible = MutableLiveData<Boolean>()
    val isCheckOutEligible: LiveData<Boolean> get() = _isCheckOutEligible

    fun addVisitCheckIn(
        activity: Activity,
        visitsDetails: VisitDetailsResponse.Data,
        normalCheckInOut: Boolean,
        alertType: String,
    ) {
        _isLoading.value = true
        val actualStartTime = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
        // Split into date and time
        val parts = actualStartTime.split(" ")
        val datePart = parts[0] // yyyy-MM-dd
        val timePart = parts[1] // HH:mm:ss
        viewModelScope.launch {
            try {
                val userData =
                    sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)?.let {
                        Gson().fromJson(it, OtpVerifyResponse.Data::class.java)
                    } ?: return@launch

                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                    dbRepository.updateVisitCheckInTimesAndSync(
                        visitDetailsId = visitsDetails.visitDetailsId,
                        actualStartTime = timePart,
                        actualStartTimeString = actualStartTime,
                        visitStatus = "In Progress",
                        checkInSync = true,
                    )

                    val uatId = dbRepository.getUatId(visitDetailsId = visitsDetails.visitDetailsId)

                    _addVisitCheckInResponseStart.value = listOf(
                        AddVisitCheckInResponse.Data(
                            actual_end_time = "",
                            actual_start_time = actualStartTime,
                            client_id = visitsDetails.clientId,
                            created_at = actualStartTime,
                            id = uatId,
                            status = "In Progress",
                            updated_at = actualStartTime,
                            user_id = userData.id,
                            visit_details_id = visitsDetails.visitDetailsId,
                        )
                    )
                    // print response value
                    AlertUtils.showLog("Response", _addVisitCheckInResponseStart.value.toString())
                    return@launch
                }

                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.addVisitCheckIn(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = visitsDetails.clientId,
                    visitDetailsId = visitsDetails.visitDetailsId,
                    userId = userData.id,
                    status = "checkin",
                    actualStartTime = actualStartTime,
                    createdAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        AlertUtils.showToast(activity, list.message, ToastyType.SUCCESS)
                        _addVisitCheckInResponseStart.value = list.data
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
                //_addVisitCheckInResponse.value = _addVisitCheckInResponseStart.value
                try {
                    val alertJobs = mutableListOf<Deferred<Boolean>>()

                    if (!normalCheckInOut) {
                        val job1 = viewModelScope.async {
                            automaticAlerts(
                                activity,
                                uatId = _addVisitCheckInResponseStart.value!![0].id,
                                visitDetailsId = visitsDetails.visitDetailsId,
                                clientId = visitsDetails.clientId,
                                actualStartTime = visitsDetails.plannedStartTime,
                                startTime = actualStartTime,
                                actualEndTime = visitsDetails.plannedEndTime,
                                endTime = "",
                                checkInOut = "checkin",
                                isSchedule = visitsDetails.visitType != "Unscheduled",
                                alertType = "Force Check-In"
                            )
                        }
                        alertJobs.add(job1)
                    }

                    if (alertType.isNotEmpty()) {
                        val job2 = viewModelScope.async {
                            automaticAlerts(
                                activity,
                                uatId = _addVisitCheckInResponseStart.value!![0].id,
                                visitDetailsId = visitsDetails.visitDetailsId,
                                clientId = visitsDetails.clientId,
                                actualStartTime = visitsDetails.plannedStartTime,
                                startTime = actualStartTime,
                                actualEndTime = visitsDetails.plannedEndTime,
                                endTime = "",
                                checkInOut = "checkin",
                                isSchedule = visitsDetails.visitType != "Unscheduled",
                                alertType = alertType
                            )
                        }
                        alertJobs.add(job2)
                    }

                    // Wait for both alert API calls to complete
                    alertJobs.awaitAll()

                    // Only then update list and LiveData
                    _addVisitCheckInResponse.value = _addVisitCheckInResponseStart.value

                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun automaticAlerts(
        activity: Activity,
        uatId: String,
        visitDetailsId: String,
        clientId: String,
        actualStartTime: String,
        startTime: String,
        actualEndTime: String,
        endTime: String,
        checkInOut: String,
        isSchedule: Boolean,
        alertType: String
    ): Boolean {
        // print how many time is coming
        AlertUtils.showLog("Automatic Alert called", "***********")
        AlertUtils.showLog("Automatic Alert called", uatId)
        AlertUtils.showLog("Automatic Alert called", visitDetailsId)
        AlertUtils.showLog("Automatic Alert called", clientId)
        AlertUtils.showLog("Automatic Alert called", alertType)
        AlertUtils.showLog("Automatic Alert called", "***********")
        return try {
            val gson = Gson()
            val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
            val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

            if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                val autoAlertEntity = AutoAlertEntity(
                    colId = System.currentTimeMillis().toString(),
                    visitDetailsId = visitDetailsId,
                    clientId = clientId,
                    uatId = uatId,
                    userId = userData.id,
                    alertType = alertType,
                    alertStatus = "Action Required",
                    createdAt = DateTimeUtils.getCurrentTimestampGMT(),
                    alertAction = 1,
                    todoDetailsId = null,
                    scheduledId = null,
                    blisterPackId = null,
                    alertSync = true,
                )
                dbRepository.insertAutoAlerts(autoAlertEntity = autoAlertEntity)
                true
            }

            val response = repository.automaticAlerts(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                uatId = uatId,
                visitDetailsId = visitDetailsId,
                clientId = clientId,
                alertType = alertType,
                alertStatus = "Action Required",
                createdAt = DateTimeUtils.getCurrentTimestampGMT(),
                userId = userData.id
            )

            if (response.isSuccessful) {
                true
            } else {
                // You can still log or handle error
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun checkOutEligible(activity: Activity, visitDetailsId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                    withContext(Dispatchers.IO) {
                        val todosList =
                            dbRepository.getTodosWithEssentialAndEmptyOutcome(visitDetailsId)
                        val medsList = dbRepository.getMedicationsWithScheduled(visitDetailsId)

                        if (todosList.isNotEmpty() || medsList.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                AlertUtils.showToast(
                                    activity,
                                    "Please complete all essential tasks before checkout",
                                    ToastyType.ERROR
                                )
                            }
                            return@withContext
                        }
                        withContext(Dispatchers.Main) {
                            _isCheckOutEligible.value = true
                        }
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
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    _isCheckOutEligible.value = true
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

    fun updateVisitCheckOut(
        activity: Activity,
        visitsDetails: VisitDetailsResponse.Data,
        normalCheckInOut: Boolean,
        alertType: String,
    ) {
        _isLoading.value = true
        val actualEndTime = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
        // Split into date and time
        val parts = actualEndTime.split(" ")
        val datePart = parts[0] // yyyy-MM-dd
        val timePart = parts[1] // HH:mm:ss
        viewModelScope.launch {
            try {
                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                    val actualStartTimeString = dbRepository.getActualStartTimeString(visitDetailsId = visitsDetails.visitDetailsId)
                    // need total time between actualEndTime and actualStartTimeString (both format is yyyy-MM-dd HH:mm:ss)
                    val formattedDiff = DateTimeUtils.calculateTimeDifference(actualStartTimeString, actualEndTime)

                    dbRepository.updateVisitCheckOutTimesAndSync(
                        visitDetailsId = visitsDetails.visitDetailsId,
                        actualEndTime = timePart,
                        actualEndTimeString = actualEndTime,
                        totalActualTimeDiff = formattedDiff,
                        visitStatus = "Completed",
                        checkOutSync = true,
                    )

                    val uatId = dbRepository.getUatId(visitDetailsId = visitsDetails.visitDetailsId)

                    _updateVisitCheckoutResponseStart.value = listOf(
                        UpdateVisitCheckoutResponse.Data(
                            actual_end_time = actualEndTime,
                            actual_start_time = "",
                            client_id = visitsDetails.clientId,
                            created_at = actualEndTime,
                            id = uatId,
                            status = "Completed",
                            updated_at = actualEndTime,
                            user_id = userData.id,
                            visit_details_id = visitsDetails.visitDetailsId,
                        )
                    )
                    // print response value
                    AlertUtils.showLog("Response", _updateVisitCheckoutResponseStart.value.toString())
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

                val response = repository.updateVisitCheckout(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id,
                    visitDetailsId = visitsDetails.visitDetailsId,
                    //actualEndTime = URLDecoder.decode(DateTimeUtils.getCurrentTimeAndSecGMT(), "UTF-8"),
                    actualEndTime = actualEndTime,
                    status = "checkout",
                    updatedAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        AlertUtils.showToast(activity, list.message, ToastyType.SUCCESS)
                        _updateVisitCheckoutResponseStart.value = list.data
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

                try {
                    val alertJobs = mutableListOf<Deferred<Boolean>>()

                    val responseList = _updateVisitCheckoutResponseStart.value
                    val responseItem = responseList?.firstOrNull()  // Safer than using `!!`

                    if (responseItem != null) {
                        // Force Check-Out alert
                        if (!normalCheckInOut) {
                            val job1 = viewModelScope.async {
                                automaticAlerts(
                                    activity = activity,
                                    uatId = responseItem.id,
                                    visitDetailsId = visitsDetails.visitDetailsId,
                                    clientId = visitsDetails.clientId,
                                    actualStartTime = visitsDetails.plannedStartTime,
                                    startTime = "",
                                    actualEndTime = visitsDetails.plannedEndTime,
                                    endTime = actualEndTime,
                                    checkInOut = "checkout",
                                    isSchedule = visitsDetails.visitType != "Unscheduled",
                                    alertType = "Force Check-Out",
                                )
                            }
                            alertJobs.add(job1)
                        }

                        // Conditional alert
                        if (alertType.isNotEmpty()) {
                            val job2 = viewModelScope.async {
                                automaticAlerts(
                                    activity = activity,
                                    uatId = responseItem.id,
                                    visitDetailsId = visitsDetails.visitDetailsId,
                                    clientId = visitsDetails.clientId,
                                    actualStartTime = visitsDetails.plannedStartTime,
                                    startTime = "",
                                    actualEndTime = visitsDetails.plannedEndTime,
                                    endTime = actualEndTime,
                                    checkInOut = "checkout",
                                    isSchedule = visitsDetails.visitType != "Unscheduled",
                                    alertType = alertType,
                                )
                            }
                            alertJobs.add(job2)
                        }

                        // Wait for all parallel alert jobs to complete
                        alertJobs.awaitAll()

                        // Then update LiveData
                        _updateVisitCheckoutResponse.value = responseList
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    AlertUtils.showLog(
                        "Automatic Alert Error",
                        e.localizedMessage ?: "Unknown error"
                    )
                } finally {
                    _isLoading.value = false // Just in case
                }
            }
        }
    }

    fun createUnscheduledVisit(activity: Activity, clientId: String, normalCheckInOut: Boolean) {
        _isLoading.value = true
        _isAutoCheckIn.value = false
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

                val response = repository.addUnscheduledVisits(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    userId = userData.id.toString(),
                    clientId = clientId,
                    visitDate = DateTimeUtils.getCurrentDateGMT(),
                    actualStartTime = DateTimeUtils.getCurrentTimestampForCheckOutGMT(),
                    createdAt = DateTimeUtils.getCurrentTimestampGMT(),
                )

                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _userActualTimeData.value = list.userActualTimeData[0]
                    }
                } else {
                    _isAutoCheckIn.value = true
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
                if (!normalCheckInOut) {
                    _userActualTimeData.value?.let { data ->
                        automaticAlerts(
                            activity = activity,
                            uatId = data.id,
                            visitDetailsId = data.visit_details_id,
                            clientId = data.client_id,
                            actualStartTime = "",
                            startTime = "",
                            actualEndTime = "",
                            endTime = "",
                            checkInOut = "checkin",
                            isSchedule = false,
                            alertType = "Force Check-In"
                        )
                    }
                }
            }
        }
    }

    fun verifyQrCode(activity: Activity, clientId: String, scanResult: String) {
        _isLoading.value = true
        _isAutoCheckIn.value = false
        viewModelScope.launch {
            try {
                if(!NetworkUtils.isNetworkAvailable(activity) && sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {

                    val verified = dbRepository.validateQrCode(clientId, scanResult)
                    _isLoading.value = false
                    if (verified) {
                        _qrVerified.value = true
                        return@launch
                    } else {
                        _isAutoCheckIn.value = true
                        AlertUtils.showToast(
                            activity,
                            "Invalid QR Code",
                            ToastyType.ERROR
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

                val gson = Gson()
                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

                val response = repository.verifyQrCode(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = clientId,
                    qrcodeToken = scanResult
                )

                _isLoading.value = false
                if (response.isSuccessful) {
                    _qrVerified.value = true
                } else {
                    _isAutoCheckIn.value = true
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

    private val _markerPosition = MutableLiveData<LatLng>()
    val markerPosition: LiveData<LatLng> get() = _markerPosition

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    fun fetchCurrentLocation() {
        // Ensure location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Use the location object
                    val latLng = LatLng(location.latitude, location.longitude)
                    _markerPosition.value = latLng
                } else {
                    // If lastLocation is null, request a new location update
                    requestNewLocation()
                }
            }.addOnFailureListener { exception ->
                // Handle exception
                println("Error fetching last location: $exception")
            }

        }
    }

    private fun requestNewLocation() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000 // Get updates as frequently as 5 seconds
            numUpdates = 1 // Request only a single update
        }

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val newLocation = locationResult.lastLocation
                if (newLocation != null) {
                    val latLng = LatLng(newLocation.latitude, newLocation.longitude)
                    _markerPosition.value = latLng
                } else {
                    println("Failed to get new location")
                }
            }
        }

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
}