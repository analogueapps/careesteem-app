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
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.unscheduled_visits.model.UpdateVisitCheckoutResponse
import com.aits.careesteem.view.visits.model.AddVisitCheckInResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
): AndroidViewModel(application) {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for UI
    private val _isAutoCheckIn = MutableLiveData<Boolean>(true)
    val isAutoCheckIn: LiveData<Boolean> get() = _isAutoCheckIn

    // LiveData for UI
    private val _qrVerified = MutableLiveData<Boolean>()
    val qrVerified: LiveData<Boolean> get() = _qrVerified

    private val _addVisitCheckInResponseStart = MutableLiveData<List<AddVisitCheckInResponse.Data>>()
    val addVisitCheckInResponseStart: LiveData<List<AddVisitCheckInResponse.Data>> get() = _addVisitCheckInResponseStart

    private val _addVisitCheckInResponse = MutableLiveData<List<AddVisitCheckInResponse.Data>>()
    val addVisitCheckInResponse: LiveData<List<AddVisitCheckInResponse.Data>> get() = _addVisitCheckInResponse

    private val _updateVisitCheckoutResponse = MutableLiveData<List<UpdateVisitCheckoutResponse.Data>>()
    val updateVisitCheckoutResponse: LiveData<List<UpdateVisitCheckoutResponse.Data>> get() = _updateVisitCheckoutResponse

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
        val actualStartTime = DateTimeUtils.getCurrentTimeGMT()
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

                val response = repository.addVisitCheckIn(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    clientId = visitsDetails.clientId,
                    visitDetailsId = visitsDetails.visitDetailsId,
                    userId = userData.id,
                    status = "checkin",
                    actualStartTime = actualStartTime,
                    createdAt = DateTimeUtils.getCurrentTimestampGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        AlertUtils.showToast(activity, list.message)
                        _addVisitCheckInResponseStart.value = list.data
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
                if(!normalCheckInOut) {
                   // automaticAlerts(activity = activity, visitsDetails = visitsDetails)

                    automaticAlerts(
                        activity = activity,
                        uatId = _addVisitCheckInResponseStart.value!![0].id,
                        visitDetailsId = visitsDetails.visitDetailsId,
                        clientId = visitsDetails.clientId,
                        actualStartTime = visitsDetails.plannedStartTime,
                        startTime = actualStartTime,
                        actualEndTime = visitsDetails.plannedEndTime,
                        endTime = "",
                        checkInOut = "checkin",
                        isSchedule = visitsDetails.visitType != "Unscheduled",
                        alertType = "Force Check In"
                    )

                    if(alertType.isNotEmpty()) {
                        automaticAlerts(
                            activity = activity,
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
                } else {
                    _addVisitCheckInResponse.value = _addVisitCheckInResponseStart.value
                }
            }
        }
    }

    private fun automaticAlerts(
        activity: Activity,
        uatId: Int,
        visitDetailsId: Int,
        clientId: Int,
        actualStartTime: String,
        startTime: String,
        actualEndTime: String,
        endTime: String,
        checkInOut: String,
        isSchedule: Boolean,
        alertType: String
    ) {
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

//                var alertType = ""
//                if(checkInOut == "checkin") {
//                    alertType = if(isSchedule) {
//                        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//                        val givenTime = LocalTime.parse(actualStartTime, formatter)
//                        val currentUtcTime = LocalTime.parse(startTime, formatter)
//                        when {
//                            currentUtcTime.isBefore(givenTime) -> "Early Check In"
//                            currentUtcTime.isAfter(givenTime) -> "Late Check In"
//                            else -> "Force Check In"
//                        }
//                    } else {
//                        "Force Check In"
//                    }
//                } else if(checkInOut == "checkout") {
//                    alertType = if(isSchedule) {
//                        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//                        val givenTime = LocalTime.parse(actualEndTime, formatter)
//                        val currentUtcTime = LocalTime.parse(endTime, formatter)
//                        when {
//                            currentUtcTime.isBefore(givenTime) -> "Early Check Out"
//                            currentUtcTime.isAfter(givenTime) -> "Late Check Out"
//                            else -> "Force Check Out"
//                        }
//                    } else {
//                        "Force Check Out"
//                    }
//                }

                val response = repository.automaticAlerts(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    uatId = uatId,
                    visitDetailsId = visitDetailsId,
                    clientId = clientId,
                    alertType = alertType,
                    alertStatus = "Action Required",
                    createdAt = DateTimeUtils.getCurrentTimestampGMT()
                )

                if (response.isSuccessful) {
                    //_isCheckOutEligible.value = true
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showLog("activity","Request Timeout. Please try again.")
            } catch (e: HttpException) {
                AlertUtils.showLog("activity", "Server error: ${e.message}")
            } catch (e: Exception) {
                AlertUtils.showLog("activity","An error occurred: ${e.message}")
                e.printStackTrace()
            } finally {
                if(alertType != "Force Check In") {
                    _addVisitCheckInResponse.value = _addVisitCheckInResponseStart.value
                }
            }
        }
    }

    fun checkOutEligible(activity: Activity, visitDetailsId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(activity, "No Internet Connection. Please check your network and try again.")
                    return@launch
                }

                val response = repository.checkOutEligible(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    _isCheckOutEligible.value = true
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

    fun updateVisitCheckOut(
        activity: Activity,
        visitsDetails: VisitDetailsResponse.Data,
        normalCheckInOut: Boolean,
        alertType: String,
    ) {
        _isLoading.value = true
        val actualEndTime = DateTimeUtils.getCurrentTimeGMT()
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

                val response = repository.updateVisitCheckout(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    userId = userData.id,
                    visitDetailsId = visitsDetails.visitDetailsId,
                    //actualEndTime = URLDecoder.decode(DateTimeUtils.getCurrentTimeAndSecGMT(), "UTF-8"),
                    actualEndTime = actualEndTime,
                    status = "checkout",
                    updatedAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        AlertUtils.showToast(activity, list.message)
                        _updateVisitCheckoutResponse.value = list.data
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
                if(!normalCheckInOut) {
                    // automaticAlerts(activity = activity, visitsDetails = visitsDetails)
                    automaticAlerts(
                        activity = activity,
                        uatId = _updateVisitCheckoutResponse.value!![0].id,
                        visitDetailsId = visitsDetails.visitDetailsId,
                        clientId = visitsDetails.clientId,
                        actualStartTime = visitsDetails.plannedStartTime,
                        startTime = "",
                        actualEndTime = visitsDetails.plannedEndTime,
                        endTime = actualEndTime,
                        checkInOut = "checkout",
                        isSchedule = visitsDetails.visitType != "Unscheduled",
                        alertType = "Force Check Out",
                    )

                    if(alertType.isNotEmpty()) {
                        automaticAlerts(
                            activity = activity,
                            uatId = _updateVisitCheckoutResponse.value!![0].id,
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
                }
            }
        }
    }

    fun createUnscheduledVisit(activity: Activity, clientId: Int, normalCheckInOut: Boolean) {
        _isLoading.value = true
        _isAutoCheckIn.value = false
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

                val response = repository.addUnscheduledVisits(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
                    userId = userData.id.toString(),
                    clientId = clientId,
                    visitDate = DateTimeUtils.getCurrentDateGMT(),
                    actualStartTime = DateTimeUtils.getCurrentTimeGMT(),
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
                AlertUtils.showToast(activity,"Request Timeout. Please try again.")
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}")
            } catch (e: Exception) {
                AlertUtils.showToast(activity,"An error occurred: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                if(!normalCheckInOut) {
                    automaticAlerts(
                        activity = activity,
                        uatId = _userActualTimeData.value!!.id,
                        visitDetailsId = _userActualTimeData.value!!.visit_details_id,
                        clientId = _userActualTimeData.value!!.client_id,
                        actualStartTime = "",
                        startTime = "",
                        actualEndTime = "",
                        endTime = "",
                        checkInOut = "checkin",
                        isSchedule = false,
                        alertType = "Force Check In"
                    )
                }
            }
        }
    }

    fun verifyQrCode(activity: Activity, clientId: Int, scanResult: String) {
        _isLoading.value = true
        _isAutoCheckIn.value = false
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

                val response = repository.verifyQrCode(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).toString(),
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