/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.alerts.model.AlertListResponse
import com.aits.careesteem.view.alerts.model.ClientNameListResponse
import com.aits.careesteem.view.auth.model.CreateHashToken
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.model.UploadedDocumentsResponse
import com.aits.careesteem.view.notification.model.ClearNotificationRequest
import com.aits.careesteem.view.notification.model.NotificationListResponse
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
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(private val apiService: ApiService) {
    suspend fun sendOtpUserLogin(
        contactNumber: String,
        telephoneCodes: String,
    ): Response<SendOtpUserLoginResponse> {
        return apiService.sendOtpUserLogin(
            contactNumber = contactNumber,
            telephoneCodes = telephoneCodes
        )
    }

    suspend fun verifyOtp(
        contactNumber: String,
        countryCode: String,
        otp: Int,
        hashToken: String,
        fcmToken: String
    ): Response<OtpVerifyResponse> {
        return apiService.verifyOtp(
            contactNumber = contactNumber,
            countryCode = countryCode,
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

    suspend fun selectDbName(
        agencyId: String,
        contactNumber: String,
        userId: String,
    ): Response<CreateHashToken> {
        return apiService.selectDbName(
            contactNumber = contactNumber,
            agencyId = agencyId,
            userId = userId,
        )
    }

    suspend fun createPasscode(
        hashToken: String,
        contactNumber: String,
        passcode: Int,
    ): Response<JsonObject> {
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
        userId: String,
        visitDate: String,
    ): Response<VisitListResponse> {
        return apiService.getVisitList(
            hashToken = hashToken,
            userId = userId,
            visitDate = visitDate,
        )
    }

    suspend fun getVisitDetails(
        hashToken: String,
        visitDetailsId: String,
        userId: String,
    ): Response<VisitDetailsResponse> {
        return apiService.getVisitDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            userId = userId,
        )
    }

    suspend fun getClientsList(hashToken: String): Response<ClientsList> {
        return apiService.getClientsList(hashToken = hashToken)
    }

    suspend fun getClientDetails(
        hashToken: String,
        clientId: String
    ): Response<ClientDetailsResponse> {
        return apiService.getClientDetails(
            hashToken = hashToken,
            clientId = clientId,
        )
    }

    suspend fun getClientCarePlanAss(
        hashToken: String,
        clientId: String
    ): Response<ClientCarePlanAssessment> {
        return apiService.getClientCarePlanAss(
            hashToken = hashToken,
            clientId = clientId
        )
    }

    suspend fun getClientCarePlanRiskAss(
        hashToken: String,
        clientId: String
    ): Response<CarePlanRiskAssList> {
        return apiService.getClientCarePlanRiskAss(
            hashToken = hashToken,
            clientId = clientId
        )
    }

    suspend fun getUploadedDocuments(
        hashToken: String,
        clientId: String
    ): Response<UploadedDocumentsResponse> {
        return apiService.getUploadedDocuments(
            hashToken = hashToken,
            clientId = clientId
        )
    }

    suspend fun addUnscheduledVisits(
        hashToken: String,
        userId: String,
        clientId: String,
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
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun updateTodoDetails(
        hashToken: String,
        todoId: String,
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
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun addUnscheduledTodoDetails(
        hashToken: String,
        visitDetailsId: String,
        todoUserId: String,
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
        todoId: String,
        todoUserId: String,
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
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun addUnscheduledMedicationDetails(
        hashToken: String,
        visitDetailsId: String,
        medicationUserId: String,
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
        medicationId: String,
        medicationUserId: String,
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
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun addUnscheduledVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String,
        visitUserId: String,
        visitCreatedAt: String,
        visitNotes: String
    ): Response<JsonObject> {
        return apiService.addUnscheduledVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            visitUserId = visitUserId,
            createdByUserId = visitUserId,
            visitCreatedAt = visitCreatedAt,
            visitNotes = visitNotes
        )
    }

    suspend fun updateUnscheduledVisitNotesDetails(
        hashToken: String,
        visitNotesId: String,
        visitUserId: String,
        visitNotes: String,
        visitUpdatedAt: String
    ): Response<JsonObject> {
        return apiService.updateUnscheduledVisitNotesDetails(
            hashToken = hashToken,
            visitNotesId = visitNotesId,
            updatedAt = visitUpdatedAt,
            visitNotes = visitNotes,
            updatedByUserid = visitUserId
        )
    }

    suspend fun getClientVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<ClientVisitNotesDetails> {
        return apiService.getClientVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun addClientVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: String,
        updatedByUserid: String,
        createdAt: String,
    ): Response<JsonObject> {
        return apiService.addClientVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
            visitNotes = visitNotes,
            createdByUserid = createdByUserid,
            updatedByUserid = updatedByUserid,
            createdAt = createdAt
        )
    }

    suspend fun updateVisitNotesDetail(
        hashToken: String,
        visitNotesId: String,
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: String,
        updatedByUserid: String,
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
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun getUnscheduledMedicationPrn(
        hashToken: String,
        clientId: String,
        date: String
    ): Response<MedicationDetailsListResponse> {
        return apiService.getUnscheduledMedicationPrn(
            hashToken = hashToken,
            clientId = clientId,
            date = date
        )
    }

    suspend fun medicationScheduledDetails(
        hashToken: String,
        scheduledDetailsId: String,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationScheduledDetails(
            hashToken = hashToken,
            scheduledDetailsId = scheduledDetailsId,
            status = status,
            carerNotes = carerNotes,
            scheduledOutcome = 1
        )
    }

    suspend fun medicationBpDetails(
        hashToken: String,
        blisterPackDetailsId: String,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationBpDetails(
            hashToken = hashToken,
            blisterPackDetailsId = blisterPackDetailsId,
            status = status,
            carerNotes = carerNotes,
            blisterPackOutcome = 1
        )
    }

    suspend fun updateMedicationPrn(
        hashToken: String,
        prnDetailsId: String,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.updateMedicationPrn(
            hashToken = hashToken,
            prnDetailsId = prnDetailsId,
            status = status,
            carerNotes = carerNotes
        )
    }

    suspend fun medicationPrnDetails(
        hashToken: String,
        clientId: String,
        medicationId: String,
        prnId: String,
        doesPer: Int,
        doses: Int,
        timeFrame: String,
        prnOffered: String,
        prnBeGiven: String,
        visitDetailsId: String,
        userId: String,
        medicationTime: String,
        createdAt: String,
        carerNotes: String,
        status: String,
    ): Response<JsonObject> {
        return apiService.medicationPrnDetails(
            hashToken = hashToken,
            clientId = clientId,
            medicationId = medicationId,
            prnId = prnId,
            doesPer = doesPer,
            doses = doses,
            timeFrame = timeFrame,
            prnOffered = prnOffered,
            prnBeGiven = prnBeGiven,
            visitDetailsId = visitDetailsId,
            userId = userId,
            medicationTime = medicationTime,
            createdAt = createdAt,
            carerNotes = carerNotes,
            status = status
        )
    }

    suspend fun getUserDetailsById(
        hashToken: String,
        userId: String
    ): Response<UserDetailsResponse> {
        return apiService.getUserDetailsById(
            hashToken = hashToken,
            userId = userId
        )
    }

    suspend fun addVisitCheckIn(
        hashToken: String,
        clientId: String,
        visitDetailsId: String,
        userId: String,
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

    suspend fun checkInEligible(
        hashToken: String,
        userId: String
    ): Response<JsonObject> {
        return apiService.checkInEligible(
            hashToken = hashToken,
            userId = userId
        )
    }

    suspend fun checkOutEligible(
        hashToken: String,
        visitDetailsId: String
    ): Response<JsonObject> {
        return apiService.checkOutEligible(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId
        )
    }

    suspend fun updateVisitCheckout(
        hashToken: String,
        userId: String,
        visitDetailsId: String,
        actualEndTime: String,
        status: String,
        updatedAt: String
    ): Response<UpdateVisitCheckoutResponse> {
        return apiService.updateVisitCheckout(
            hashToken = hashToken,
            userId = userId,
            visitDetailsId = visitDetailsId,
            actualEndTime = actualEndTime,
            status = status,
            updatedAt = updatedAt
        )
    }

    suspend fun automaticAlerts(
        hashToken: String,
        uatId: String,
        visitDetailsId: String,
        clientId: String,
        alertType: String,
        alertStatus: String,
        createdAt: String,
        userId: String,
    ): Response<JsonObject> {
        return apiService.automaticAlerts(
            hashToken = hashToken,
            uatId = uatId,
            visitDetailsId = visitDetailsId,
            clientId = clientId,
            alertType = alertType,
            alertStatus = alertStatus,
            createdAt = createdAt,
            userId = userId
        )
    }

    suspend fun automaticTodoAlerts(
        hashToken: String,
        todoDetailsId: String,
        visitDetailsId: String,
        clientId: String,
        alertType: String,
        alertStatus: String,
        createdAt: String
    ): Response<JsonObject> {
        return apiService.automaticTodoAlerts(
            hashToken = hashToken,
            todoDetailsId = todoDetailsId,
            visitDetailsId = visitDetailsId,
            clientId = clientId,
            alertType = alertType,
            alertStatus = alertStatus,
            createdAt = createdAt
        )
    }

    suspend fun automaticMedicationAlerts(
        hashToken: String,
        scheduledId: Any,
        blisterPackId: Any,
        visitDetailsId: String,
        clientId: String,
        alertType: String,
        alertStatus: String,
        createdAt: String
    ): Response<JsonObject> {
        return apiService.automaticMedicationAlerts(
            hashToken = hashToken,
            scheduledId = scheduledId,
            blisterPackId = blisterPackId,
            visitDetailsId = visitDetailsId,
            clientId = clientId,
            alertType = alertType,
            alertStatus = alertStatus,
            createdAt = createdAt
        )
    }

    suspend fun verifyQrCode(
        hashToken: String,
        clientId: String,
        qrcodeToken: String
    ): Response<JsonObject> {
        return apiService.verifyQrCode(
            hashToken = hashToken,
            clientId = clientId,
            qrcodeToken = qrcodeToken
        )
    }

    suspend fun getClientsListAlerts(
        hashToken: String,
        userId: String,
        visitDate: String,
    ): Response<ClientNameListResponse> {
        return apiService.getClientsList(
            hashToken = hashToken,
            userId = userId,
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

        val imageParts = images.mapNotNull { imageFile ->
            if (imageFile.exists() && imageFile.name.isNotBlank()) {
                val mediaType = "image/png".toMediaTypeOrNull()
                val requestBody = imageFile.asRequestBody(mediaType)
                MultipartBody.Part.createFormData("images", imageFile.name, requestBody)
            } else {
                println("Invalid or missing file: ${imageFile.path}")
                null
            }
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
            images = imageParts,
            alertsStatus = AppConstant.createRequestBody("Action Required")
        )
    }

    suspend fun getAlertsList(
        userId: String,
        hashToken: String
    ): Response<AlertListResponse> {
        return apiService.getAlertsList(
            userId = userId,
            hashToken = hashToken
        )
    }

    suspend fun getNotificationList(
        userId: String,
        hashToken: String
    ): Response<NotificationListResponse> {
        return apiService.getNotificationList(
            userId = userId,
            hashToken = hashToken
        )
    }

    suspend fun clearNotification(
        request: ClearNotificationRequest,
        hashToken: String
    ): Response<JsonObject> {
        return apiService.clearNotification(
            request = request,
            hashToken = hashToken
        )
    }

    suspend fun getClientPreviousVisitNotesDetails(
        hashToken: String,
        visitDetailsId: String
    ): Response<ClientVisitNotesDetails> {
        return apiService.getClientPreviousVisitNotesDetails(
            hashToken = hashToken,
            visitDetailsId = visitDetailsId,
        )
    }
}