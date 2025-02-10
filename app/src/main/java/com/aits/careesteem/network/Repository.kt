/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
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
    ): Response<OtpVerifyResponse> {
        return apiService.verifyOtp(
            contactNumber = contactNumber,
            otp = otp
        )
    }

    suspend fun createPasscode(
        contactNumber: String,
        passcode: Int,
    ): Response<OtpVerifyResponse> {
        return apiService.createPasscode(
            contactNumber = contactNumber,
            passcode = passcode
        )
    }

    suspend fun forgotPasscode(
        contactNumber: String,
        telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse> {
        return apiService.forgotPasscode(
            contactNumber = contactNumber,
            telephoneCodes = telephoneCodes
        )
    }

    suspend fun resetPasscode(
        contactNumber: String,
        otp: Int,
        passcode: Int,
    ): Response<OtpVerifyResponse> {
        return apiService.resetPasscode(
            contactNumber = contactNumber,
            otp = otp,
            passcode = passcode
        )
    }

    suspend fun getVisitList(
        id: Int,
        visitDate: String,
    ): Response<VisitListResponse> {
        return apiService.getVisitList(
            id = id,
            visitDate = visitDate
        )
    }

    suspend fun getClientsList(): Response<ClientsList> {
        return apiService.getClientsList()
    }

    suspend fun getClientDetails(clientId: Int): Response<ClientDetailsResponse> {
        return apiService.getClientDetails(
            clientId = clientId
        )
    }

    suspend fun getClientCarePlanRiskAss(clientId: Int): Response<CarePlanRiskAssList> {
        return apiService.getClientCarePlanRiskAss(
            clientId = clientId
        )
    }

    suspend fun addUnscheduledVisits(
        userId: String,
        clientId: Int,
        visitDate: String,
        actualStartTime: String,
        createdAt: String,
    ): Response<AddUvVisitResponse> {
        return apiService.addUnscheduledVisits(
            userId = userId,
            clientId = clientId,
            visitDate = visitDate,
            actualStartTime = actualStartTime,
            createdAt = createdAt
        )
    }

    suspend fun getToDoList(visitDetailsId: String): Response<TodoListResponse> {
        return apiService.getToDoList(
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun getUnscheduledTodoDetails(visitDetailsId: String): Response<UvTodoListResponse> {
        return apiService.getUnscheduledTodoDetails(
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addUnscheduledTodoDetails(
        visitDetailsId: Int,
        todoUserId: Int,
        todoCreatedAt: String,
        todoNotes: String
    ): Response<JsonObject> {
        return apiService.addUnscheduledTodoDetails(
            visitDetailsId = visitDetailsId,
            todoUserId = todoUserId,
            todoCreatedAt = todoCreatedAt,
            todoNotes = todoNotes
        )
    }

    suspend fun updateUnscheduledTodoDetails(
        todoId: Int,
        todoUserId: Int,
        todoNotes: String,
        todoUpdatedAt: String
    ): Response<JsonObject> {
        return apiService.updateUnscheduledTodoDetails(
            todoId = todoId,
            todoUserId = todoUserId,
            todoNotes = todoNotes,
            todoUpdatedAt = todoUpdatedAt
        )
    }

    suspend fun updateTodoDetails(
        todoId: Int,
        carerNotes: String,
        todoOutcome: Int
    ): Response<JsonObject> {
        return apiService.updateTodoDetails(
            todoId = todoId,
            carerNotes = carerNotes,
            todoOutcome = todoOutcome
        )
    }

    suspend fun getClientVisitNotesDetails(visitDetailsId: String): Response<ClientVisitNotesDetails> {
        return apiService.getClientVisitNotesDetails(
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun addClientVisitNotesDetails(
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: Int,
        updatedByUserid: Int
    ): Response<JsonObject> {
        return apiService.addClientVisitNotesDetails(
            visitDetailsId = visitDetailsId,
            visitNotes = visitNotes,
            createdByUserid = createdByUserid,
            updatedByUserid = updatedByUserid
        )
    }

    suspend fun updateVisitNotesDetail(
        visitNotesId: Int,
        visitDetailsId: String,
        visitNotes: String,
        createdByUserid: Int,
        updatedByUserid: Int
    ): Response<JsonObject> {
        return apiService.updateVisitNotesDetail(
            visitNotesId = visitNotesId,
            visitDetailsId = visitDetailsId,
            visitNotes = visitNotes,
            createdByUserid = createdByUserid,
            updatedByUserid = updatedByUserid
        )
    }

    suspend fun getMedicationDetails(visitDetailsId: String): Response<MedicationDetailsListResponse> {
        return apiService.getMedicationDetails(
            visitDetailsId = visitDetailsId.toInt()
        )
    }

    suspend fun medicationScheduledDetails(
        scheduledDetailsId: Int,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationScheduledDetails(
            scheduledDetailsId = scheduledDetailsId,
            status = status,
            carerNotes = carerNotes
        )
    }

    suspend fun medicationBpDetails(
        blisterPackDetailsId: Int,
        status: String,
        carerNotes: String
    ): Response<JsonObject> {
        return apiService.medicationBpDetails(
            blisterPackDetailsId = blisterPackDetailsId,
            status = status,
            carerNotes = carerNotes
        )
    }
}