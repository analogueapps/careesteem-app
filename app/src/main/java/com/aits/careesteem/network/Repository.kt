/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
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

    suspend fun getToDoList(visitDetailsId: String): Response<TodoListResponse> {
        return apiService.getToDoList(
            visitDetailsId = visitDetailsId.toInt()
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

    suspend fun getMedicationDetails(visitDetailsId: String): Response<JsonObject> {
        return apiService.getMedicationDetails(
            visitDetailsId = visitDetailsId.toInt()
        )
    }
}