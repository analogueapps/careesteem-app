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
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("send-otp-user-login")
    suspend fun sendOtpUserLogin(
        @Field("contact_number") contactNumber: String,
        @Field("telephone_codes") telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse>

    @FormUrlEncoded
    @POST("verify-otp")
    suspend fun verifyOtp(
        @Field("contact_number") contactNumber: String,
        @Field("otp") otp: Int,
    ): Response<OtpVerifyResponse>

    @FormUrlEncoded
    @POST("create-passcode")
    suspend fun createPasscode(
        @Field("contact_number") contactNumber: String,
        @Field("passcode") passcode: Int,
    ): Response<OtpVerifyResponse>

    @FormUrlEncoded
    @POST("forgot-passcode")
    suspend fun forgotPasscode(
        @Field("contact_number") contactNumber: String,
        @Field("telephone_codes") telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse>

    @FormUrlEncoded
    @POST("reset-passcode")
    suspend fun resetPasscode(
        @Field("contact_number") contactNumber: String,
        @Field("otp") otp: Int,
        @Field("passcode") passcode: Int,
    ): Response<OtpVerifyResponse>

    @GET("getVisitList/{id}")
    suspend fun getVisitList(
        @Path("id") id: Int,
        @Query("visit_date") visitDate: String
    ): Response<VisitListResponse>

    @GET("get-all-clients")
    suspend fun getClientsList(): Response<ClientsList>

    @GET("get-client-details/{clientId}")
    suspend fun getClientDetails(
        @Path("clientId") clientId: Int,
    ): Response<ClientDetailsResponse>

    @GET("get-client-careplan-risk-ass")
    suspend fun getClientCarePlanRiskAss(
        @Query("client_id") clientId: Int
    ): Response<CarePlanRiskAssList>

    @FormUrlEncoded
    @POST("addUnscheduledVisits")
    suspend fun addUnscheduledVisits(
        @Field("user_id") userId: String,
        @Field("client_id") clientId: Int,
        @Field("visit_date") visitDate: String,
        @Field("actual_start_time") actualStartTime: String,
        @Field("created_at") createdAt: String,
    ): Response<AddUvVisitResponse>

    @GET("gettododetails/{visitDetailsId}")
    suspend fun getToDoList(
        @Path("visitDetailsId") visitDetailsId: Int,
    ): Response<TodoListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledTodoDetails")
    suspend fun addUnscheduledTodoDetails(
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("todo_user_id") todoUserId: Int,
        @Field("todo_created_at") todoCreatedAt: String,
        @Field("todo_notes") todoNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledTodoDetails/{todoId}")
    suspend fun updateUnscheduledTodoDetails(
        @Path("todoId") todoId: Int,
        @Field("todo_notes") todoNotes: String,
        @Field("todo_user_id") todoUserId: Int,
        @Field("todo_updated_at") todoUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getUnscheduledTodoDetails/{visitDetailsId}")
    suspend fun getUnscheduledTodoDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
    ): Response<UvTodoListResponse>

    @FormUrlEncoded
    @PUT("updatetododetails/{todoId}")
    suspend fun updateTodoDetails(
        @Path("todoId") todoId: Int,
        @Field("carer_notes") carerNotes: String,
        @Field("todo_outcome") todoOutcome: Int,
    ): Response<JsonObject>

    @GET("getclientvisitnotesdetails/{visitDetailsId}")
    suspend fun getClientVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
    ): Response<ClientVisitNotesDetails>

    @FormUrlEncoded
    @POST("addclientvisitnotesdetails")
    suspend fun addClientVisitNotesDetails(
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: Int,
        @Field("updatedby_userid") updatedByUserid: Int,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updatevisitnotesdetails/{visitNotesId}")
    suspend fun updateVisitNotesDetail(
        @Path("visitNotesId") visitNotesId: Int,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: Int,
        @Field("updatedby_userid") updatedByUserid: Int,
    ): Response<JsonObject>

    @GET("get-medication-details/{visitDetailsId}")
    suspend fun getMedicationDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
    ): Response<MedicationDetailsListResponse>

    @FormUrlEncoded
    @PUT("medication-blister-pack/{scheduledDetailsId}")
    suspend fun medicationScheduledDetails(
        @Path("scheduledDetailsId") scheduledDetailsId: Int,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("medication-blister-pack/{blisterPackDetailsId}")
    suspend fun medicationBpDetails(
        @Path("blisterPackDetailsId") blisterPackDetailsId: Int,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>
}