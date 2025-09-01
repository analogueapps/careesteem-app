/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.UploadedDocumentsResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ClientDetailsViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _aboutClient = MutableLiveData<ClientDetailsResponse.Data.AboutData>()
    val aboutClient: LiveData<ClientDetailsResponse.Data.AboutData> get() = _aboutClient

    private val _clientMyCareNetwork =
        MutableLiveData<List<ClientDetailsResponse.Data.MyCareNetworkData>>()
    val clientMyCareNetwork: LiveData<List<ClientDetailsResponse.Data.MyCareNetworkData>> get() = _clientMyCareNetwork

    // Care plan assessment
    private val _activityAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.ActivityAssessmentData>()
    val activityAssessmentData: LiveData<ClientCarePlanAssessment.Data.ActivityAssessmentData> get() = _activityAssessmentData

    private val _environmentAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.EnvironmentAssessmentData>()
    val environmentAssessmentData: LiveData<ClientCarePlanAssessment.Data.EnvironmentAssessmentData> get() = _environmentAssessmentData

    private val _financialAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.FinancialAssessmentData>()
    val financialAssessmentData: LiveData<ClientCarePlanAssessment.Data.FinancialAssessmentData> get() = _financialAssessmentData

    private val _mentalHealthAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.MentalHealthAssessmentData>()
    val mentalHealthAssessmentData: LiveData<ClientCarePlanAssessment.Data.MentalHealthAssessmentData> get() = _mentalHealthAssessmentData

    private val _communicationAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.CommunicationAssessmentData>()
    val communicationAssessmentData: LiveData<ClientCarePlanAssessment.Data.CommunicationAssessmentData> get() = _communicationAssessmentData

    private val _personalHygieneAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.PersonalHygieneAssessmentData>()
    val personalHygieneAssessmentData: LiveData<ClientCarePlanAssessment.Data.PersonalHygieneAssessmentData> get() = _personalHygieneAssessmentData

    private val _medicationAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.MedicationAssessmentData>()
    val medicationAssessmentData: LiveData<ClientCarePlanAssessment.Data.MedicationAssessmentData> get() = _medicationAssessmentData

    private val _clinicalAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.ClinicalAssessmentData>()
    val clinicalAssessmentData: LiveData<ClientCarePlanAssessment.Data.ClinicalAssessmentData> get() = _clinicalAssessmentData

    private val _culturalSpiritualSocialRelationshipsAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.CulturalSpiritualSocialRelationshipsAssessmentData>()
    val culturalSpiritualSocialRelationshipsAssessmentData: LiveData<ClientCarePlanAssessment.Data.CulturalSpiritualSocialRelationshipsAssessmentData> get() = _culturalSpiritualSocialRelationshipsAssessmentData

    private val _behaviourAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.BehaviourAssessmentData>()
    val behaviourAssessmentData: LiveData<ClientCarePlanAssessment.Data.BehaviourAssessmentData> get() = _behaviourAssessmentData

    private val _oralCareAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.OralCareAssessmentData>()
    val oralCareAssessmentData: LiveData<ClientCarePlanAssessment.Data.OralCareAssessmentData> get() = _oralCareAssessmentData

    private val _breathingAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.BreathingAssessmentData>()
    val breathingAssessmentData: LiveData<ClientCarePlanAssessment.Data.BreathingAssessmentData> get() = _breathingAssessmentData

    private val _continenceAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.ContinenceAssessmentData>()
    val continenceAssessmentData: LiveData<ClientCarePlanAssessment.Data.ContinenceAssessmentData> get() = _continenceAssessmentData

    private val _domesticAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.DomesticAssessmentData>()
    val domesticAssessmentData: LiveData<ClientCarePlanAssessment.Data.DomesticAssessmentData> get() = _domesticAssessmentData

    private val _equipmentAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.EquipmentAssessmentData>()
    val equipmentAssessmentData: LiveData<ClientCarePlanAssessment.Data.EquipmentAssessmentData> get() = _equipmentAssessmentData

    private val _movingHandlingAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.MovingHandlingAssessmentData>()
    val movingHandlingAssessmentData: LiveData<ClientCarePlanAssessment.Data.MovingHandlingAssessmentData> get() = _movingHandlingAssessmentData

    private val _painAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.PainAssessmentData>()
    val painAssessmentData: LiveData<ClientCarePlanAssessment.Data.PainAssessmentData> get() = _painAssessmentData

    private val _sleepingAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.SleepingAssessmentData>()
    val sleepingAssessmentData: LiveData<ClientCarePlanAssessment.Data.SleepingAssessmentData> get() = _sleepingAssessmentData

    private val _skinAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.SkinAssessmentData>()
    val skinAssessmentData: LiveData<ClientCarePlanAssessment.Data.SkinAssessmentData> get() = _skinAssessmentData

    private val _nutritionHydrationAssessmentData =
        MutableLiveData<ClientCarePlanAssessment.Data.NutritionHydrationAssessmentData>()
    val nutritionHydrationAssessmentData: LiveData<ClientCarePlanAssessment.Data.NutritionHydrationAssessmentData> get() = _nutritionHydrationAssessmentData

    // Risk Assessment List
    private val _activityRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>>()
    val activityRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>> get() = _activityRiskAssessmentData

    private val _behaviourRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>>()
    val behaviourRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>> get() = _behaviourRiskAssessmentData

    private val _cOSHHRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>>()
    val cOSHHRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>> get() = _cOSHHRiskAssessmentData

    private val _equipmentRegisterData =
        MutableLiveData<List<CarePlanRiskAssList.Data.EquipmentRegisterData>>()
    val equipmentRegisterData: LiveData<List<CarePlanRiskAssList.Data.EquipmentRegisterData>> get() = _equipmentRegisterData

    private val _financialRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>>()
    val financialRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>> get() = _financialRiskAssessmentData

    private val _medicationRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>>()
    val medicationRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>> get() = _medicationRiskAssessmentData

    private val _selfAdministrationRiskAssessmentData =
        MutableLiveData<List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>>()
    val selfAdministrationRiskAssessmentData: LiveData<List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>> get() = _selfAdministrationRiskAssessmentData


    // Add uv data
    private val _userActualTimeData = MutableLiveData<AddUvVisitResponse.UserActualTimeData>()
    val userActualTimeData: LiveData<AddUvVisitResponse.UserActualTimeData> get() = _userActualTimeData

    private val _uploadedDocuments = MutableLiveData<List<UploadedDocumentsResponse.Data>>()
    val uploadedDocuments: LiveData<List<UploadedDocumentsResponse.Data>> get() = _uploadedDocuments

    fun getClientDetails(activity: Activity, clientId: String) {
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

                val response = repository.getClientDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = clientId
                )

                if (response.isSuccessful) {

                    response.body()?.let { list ->
                        _clientMyCareNetwork.value = list.data.MyCareNetwork
                        _aboutClient.value = list.data.About
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

    fun getClientCarePlanAss(activity: Activity, clientId: String) {
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

                val response = repository.getClientCarePlanAss(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = clientId
                    //clientId = "ec0f4840ccc34177821f1952"
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _activityAssessmentData.value = list.data[0].ActivityAssessment
                        _environmentAssessmentData.value = list.data[0].EnvironmentAssessment
                        _financialAssessmentData.value = list.data[0].FinancialAssessment
                        _mentalHealthAssessmentData.value = list.data[0].MentalHealthAssessment
                        _communicationAssessmentData.value = list.data[0].CommunicationAssessment
                        _personalHygieneAssessmentData.value =
                            list.data[0].PersonalHygieneAssessment
                        _medicationAssessmentData.value = list.data[0].MedicationAssessment
                        _clinicalAssessmentData.value = list.data[0].ClinicalAssessment
                        _culturalSpiritualSocialRelationshipsAssessmentData.value =
                            list.data[0].CulturalSpiritualSocialRelationshipsAssessment
                        _behaviourAssessmentData.value = list.data[0].BehaviourAssessment
                        _oralCareAssessmentData.value = list.data[0].OralCareAssessment
                        _breathingAssessmentData.value = list.data[0].BreathingAssessment
                        _continenceAssessmentData.value = list.data[0].ContinenceAssessment
                        _domesticAssessmentData.value = list.data[0].DomesticAssessment
                        _equipmentAssessmentData.value = list.data[0].EquipmentAssessment
                        _movingHandlingAssessmentData.value = list.data[0].MovingHandlingAssessment
                        _painAssessmentData.value = list.data[0].PainAssessment
                        _sleepingAssessmentData.value = list.data[0].SleepingAssessment
                        _skinAssessmentData.value = list.data[0].SkinAssessment
                        _nutritionHydrationAssessmentData.value =
                            list.data[0].NutritionHydrationAssessment
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

            }
        }
    }

    fun getClientCarePlanRiskAss(activity: Activity, clientId: String) {
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

                val response = repository.getClientCarePlanRiskAss(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = clientId
                    //clientId = "ec0f4840ccc34177821f1952"
                )

                if (response.isSuccessful) {

                    response.body()?.let { list ->
                        _activityRiskAssessmentData.value = list.data[0].ActivityRiskAssessment
                        _behaviourRiskAssessmentData.value = list.data[0].BehaviourRiskAssessment
                        _cOSHHRiskAssessmentData.value = list.data[0].COSHHRiskAssessment
                        _equipmentRegisterData.value = list.data[0].EquipmentRegister
                        _financialRiskAssessmentData.value = list.data[0].FinancialRiskAssessment
                        _medicationRiskAssessmentData.value = list.data[0].MedicationRiskAssessment
                        _selfAdministrationRiskAssessmentData.value =
                            list.data[0].SelfAdministrationRiskAssessment
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

            }
        }
    }

    fun getClientDocuments(activity: FragmentActivity, clientId: String) {
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

                val response = repository.getUploadedDocuments(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    clientId = clientId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _uploadedDocuments.value = list.data
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

            }
        }
    }

}