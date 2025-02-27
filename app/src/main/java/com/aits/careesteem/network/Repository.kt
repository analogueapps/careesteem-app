/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import android.content.Context
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.alerts.model.AlertListResponse
import com.aits.careesteem.view.alerts.model.ClientNameListResponse
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.unscheduled_visits.model.UpdateVisitCheckoutResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvVisitNotesListResponse
import com.aits.careesteem.view.visits.model.AddVisitCheckInResponse
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(private val apiService: ApiService) {
    suspend fun sendOtpUserLogin(
        contactNumber: String,
        telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse> {
        return apiService.sendOtpUserLogin(
            contactNumber = contactNumber,
            telephoneCodes = telephoneCodes
        )
    }

    suspend fun verifyOtp(
        contactNumber: String,
        otp: Int,
        hashToken: String,
        fcmToken: String
    ): Response<OtpVerifyResponse> {
        return apiService.verifyOtp(
            contactNumber = contactNumber,
            otp = otp,
            hashToken = hashToken,
            fcmToken = fcmToken
        )
    }

    suspend fun verifyPasscode(
        hashToken: String,
        contactNumber: String,
        passcode: Int,
    ): Response<JsonObject> {
        return apiService.verifyPasscode(
            contactNumber = contactNumber,
            passcode = passcode,
            hashToken = hashToken,
        )
    }

    suspend fun createPasscode(
        hashToken: String,
        contactNumber: String,
        passcode: Int,
    ): Response<OtpVerifyResponse> {
        return apiService.createPasscode(
            contactNumber = contactNumber,
            passcode = passcode,
            hashToken = hashToken,
        )
    }

    suspend fun forgotPasscode(
        hashToken: String,
        contactNumber: String,
        telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse> {
        return apiService.forgotPasscode(
            hashToken = hashToken,
            contactNumber = contactNumber,
            telephoneCodes = telephoneCodes,
        )
    }

    suspend fun resetPasscode(
        hashToken: String,
        contactNumber: String,
        otp: Int,
        passcode: Int,
    ): Response<OtpVerifyResponse> {
        return apiService.resetPasscode(
            hashToken = hashToken,
            contactNumber = contactNumber,
            otp = otp,
            passcode = passcode,
        )
    }

    suspend fun getVisitList(
        hashToken: String,
        id: Int,
        visitDate: String,
    ): Response<VisitListResponse> {
        return apiService.getVisitList(
            hashToken = hashToken,
            id = id,
            visitDate = visitDate,
        )
    }

    suspend fun getClientsList(hashToken: String): Response<ClientsList> {
        return apiService.getClientsList(hashToken = hashToken)
    }

    suspend fun getClientDetails(
        hashToken: String,
        clientId: Int
    ): Response<ClientDetailsResponse> {
        return apiService.getClientDetails(
            hashToken = hashToken,
            clientId = clientId,
        )
    }

    suspend fun getClientCarePlanAss(
        hashToken: String,
        clientId: Int
    ): Response<ClientCarePlanAssessment> {
        return apiService.getClientCarePlanAss(
            hashToken = hashToken,
            clientId = clientId
        )
    }

    suspend fun getClientCarePlanRiskAss(
        hashToken: String,
        clientId: Int
    ): Response<CarePlanRiskAssList> {
        return apiService.getClientCarePlanRiskAss(
            hashToken = hashToken,
            clientId = clientId
        )
    }

    suspend fun addUnscheduledVisits(
        hashToken: String,
        userId: String,
        clientId: Int,
        visitDate: String,
        actualStartTime: String,
        createdAt: String,
    ): Response<AddUvVisitResponse> {
        return apiService.addUnscheduledVisits(
            hashToken = hashToken,
            userId = userId,
            clientId = clientId,
            visitDate = visitDate,
            actualStartTime = actualStartTime,
            createdAt = createdAt
        )
    }

    suspend fun getToDoList(
        hashToken: String,
        visitDetailsId: String
    ): Response<TodoListResponse> {
        return apiService.getToDoList(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun updateTodoDetails(
        hashToken: String,
        todoId: Int,
        carerNotes: String,
        todoOutcome: Int
    ): Response<JsonObject> {
        return apiService.updateTodoDetails(
            hashToken = hashToken,
            todoId = todoId,
            carerNotes = carerNotes,
            todoOutcome = todoOutcome
        )
    }

    suspend fun getUnscheduledTodoDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<UvTodoListResponse> {
        return apiService.getUnscheduledTodoDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addUnscheduledTodoDetails(
        hashToken: String,
        visitDetailsId: Int,
        todoUserId: Int,
        todoCreatedAt: String,
        todoNotes: String
    ): Response<JsonObject> {
        return apiService.addUnscheduledTodoDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            todoUserId = todoUserId,
            todoCreatedAt = todoCreatedAt,
            todoNotes = todoNotes
        )
    }

    suspend fun updateUnscheduledTodoDetails(
        hashToken: String,
        todoId: Int,
        todoUserId: Int,
        todoNotes: String,
        todoUpdatedAt: String
    ): Response<JsonObject> {
        return apiService.updateUnscheduledTodoDetails(
            hashToken = hashToken,
            todoId = todoId,
            todoUserId = todoUserId,
            todoNotes = todoNotes,
            todoUpdatedAt = todoUpdatedAt
        )
    }

    suspend fun getUnscheduledMedicationDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<UvMedicationListResponse> {
        return apiService.getUnscheduledMedicationDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addUnscheduledMedicationDetails(
        hashToken: String,
        visitDetailsId: Int,
        medicationUserId: Int,
        medicationCreatedAt: String,
        medicationNotes: String
    ): Response<JsonObject> {
        return apiService.addUnscheduledMedicationDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            medicationUserId = medicationUserId,
            medicationCreatedAt = medicationCreatedAt,
            medicationNotes = medicationNotes
        )
    }

    suspend fun updateUnscheduledMedicationDetails(
        hashToken: String,
        medicationId: Int,
        medicationUserId: Int,
        medicationNotes: String,
        medicationUpdatedAt: String
    ): Response<JsonObject> {
        return apiService.updateUnscheduledMedicationDetails(
            hashToken = hashToken,
            medicationId = medicationId,
            medicationUserId = medicationUserId,
            medicationNotes = medicationNotes,
            medicationUpdatedAt = medicationUpdatedAt
        )
    }

    suspend fun getUnscheduledVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<UvVisitNotesListResponse> {
        return apiService.getUnscheduledVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addUnscheduledVisitNotesDetails(
        hashToken: String,
        visitDetailsId: Int,
        visitUserId: Int,
        visitCreatedAt: String,
        visitNotes: String
    ): Response<JsonObject> {
        return apiService.addUnscheduledVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            visitUserId = visitUserId,
            visitCreatedAt = visitCreatedAt,
            visitNotes = visitNotes
        )
    }

    suspend fun updateUnscheduledVisitNotesDetails(
        hashToken: String,
        visitNotesId: Int,
        visitUserId: Int,
        visitNotes: String,
        visitUpdatedAt: String
    ): Response<JsonObject> {
        return apiService.updateUnscheduledVisitNotesDetails(
            hashToken = hashToken,
            visitNotesId = visitNotesId,
            visitUserId = visitUserId,
            visitNotes = visitNotes,
            visitUpdatedAt = visitUpdatedAt
        )
    }

    suspend fun getClientVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<ClientVisitNotesDetails> {
        return apiService.getClientVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addClientVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: Int,
        updatedByUserid: Int
    ): Response<JsonObject> {
        return apiService.addClientVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            visitNotes = visitNotes,
            createdByUserid = createdByUserid,
            updatedByUserid = updatedByUserid
        )
    }

    suspend fun updateVisitNotesDetail(
        hashToken: String,
        visitNotesId: Int,
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: Int,
        updatedByUserid: Int,
        updatedAt: String
    ): Response<JsonObject> {
        return apiService.updateVisitNotesDetail(
            hashToken = hashToken,
            visitNotesId = visitNotesId,
            visitDetailsId = visitDetailsId,
            visitNotes = visitNotes,
            createdByUserid = createdByUserid,
            updatedByUserid = updatedByUserid,
            updatedAt = updatedAt
        )
    }

    suspend fun getMedicationDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<MedicationDetailsListResponse> {
        return apiService.getMedicationDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun medicationScheduledDetails(
        hashToken: String,
        scheduledDetailsId: Int,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationScheduledDetails(
            hashToken = hashToken,
            scheduledDetailsId = scheduledDetailsId,
            status = status,
            carerNotes = carerNotes
        )
    }

    suspend fun medicationBpDetails(
        hashToken: String,
        blisterPackDetailsId: Int,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationBpDetails(
            hashToken = hashToken,
            blisterPackDetailsId = blisterPackDetailsId,
            status = status,
            carerNotes = carerNotes
        )
    }

    suspend fun getUserDetailsById(
        hashToken: String,
        userId: Int
    ): Response<UserDetailsResponse> {
        return apiService.getUserDetailsById(
            hashToken = hashToken,
            userId = userId
        )
    }

    suspend fun addVisitCheckIn(
        hashToken: String,
        clientId: Int,
        visitDetailsId: Int,
        userId: Int,
        status: String,
        actualStartTime: String,
        createdAt: String
    ): Response<AddVisitCheckInResponse> {
        return apiService.addVisitCheckIn(
            hashToken = hashToken,
            clientId = clientId,
            visitDetailsId = visitDetailsId,
            userId = userId,
            status = status,
            actualStartTime = actualStartTime,
            createdAt = createdAt
        )
    }

    suspend fun updateVisitCheckout(
        hashToken: String,
        checkInId: Int,
        actualEndTime: String,
        updatedAt: String
    ): Response<UpdateVisitCheckoutResponse> {
        return apiService.updateVisitCheckout(
            hashToken = hashToken,
            checkInId = checkInId,
            actualEndTime = actualEndTime,
            updatedAt = updatedAt
        )
    }

    suspend fun verifyQrCode(
        hashToken: String,
        userId: Int,
        qrcodeToken: String
    ): Response<JsonObject> {
        return apiService.verifyQrCode(
            hashToken = hashToken,
            userId = userId,
            qrcodeToken = qrcodeToken
        )
    }

    suspend fun getClientsList(
        hashToken: String,
        id: Int,
        visitDate: String,
    ): Response<ClientNameListResponse> {
        return apiService.getClientsList(
            hashToken = hashToken,
            userId = id,
            visitDate = visitDate,
        )
    }

    suspend fun sendAlert(
        hashToken: String,
        clientId: String,
        userId: String,
        visitDetailsId: String,
        severityOfConcern: String,
        concernDetails: String,
        bodyPartType: String,
        bodyPartNames: String,
        fileName: String,
        createdAt: String,
        images: List<File>
    ): Response<JsonObject> {
        val imageParts = images.map { imageFile ->
            val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), imageFile)
            MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
        }

        return apiService.sendAlert(
            hashToken = hashToken,
            clientId = AppConstant.createRequestBody(clientId),
            userId = AppConstant.createRequestBody(userId),
            visitDetailsId = AppConstant.createRequestBody(visitDetailsId),
            severityOfConcern = AppConstant.createRequestBody(severityOfConcern),
            concernDetails = AppConstant.createRequestBody(concernDetails),
            bodyPartType = AppConstant.createRequestBody(bodyPartType),
            bodyPartNames = AppConstant.createRequestBody(bodyPartNames),
            fileName = AppConstant.createRequestBody(fileName),
            createdAt = AppConstant.createRequestBody(createdAt),
            images = imageParts
        )
    }

    suspend fun getAlertsList(
        userId: Int,
        hashToken: String
    ): Response<AlertListResponse> {
        return apiService.getAlertsList(
            userId = userId,
            hashToken = hashToken
        )
    }
}