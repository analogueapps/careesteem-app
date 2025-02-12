/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.unscheduled_visits.model.AddUvVisitResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvVisitNotesListResponse
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
        @Field("hash_token") hashToken: String,
    ): Response<OtpVerifyResponse>

    @FormUrlEncoded
    @POST("verify-passcode")
    suspend fun verifyPasscode(
        @Query("hash_token") hashToken: String,
        @Field("contact_number") contactNumber: String,
        @Field("passcode") passcode: Int
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("create-passcode")
    suspend fun createPasscode(
        @Query("hash_token") hashToken: String,
        @Field("contact_number") contactNumber: String,
        @Field("passcode") passcode: Int,
    ): Response<OtpVerifyResponse>

    @FormUrlEncoded
    @POST("forgot-passcode")
    suspend fun forgotPasscode(
        @Query("hash_token") hashToken: String,
        @Field("contact_number") contactNumber: String,
        @Field("telephone_codes") telephoneCodes: Int,
    ): Response<SendOtpUserLoginResponse>

    @FormUrlEncoded
    @POST("reset-passcode")
    suspend fun resetPasscode(
        @Query("hash_token") hashToken: String,
        @Field("contact_number") contactNumber: String,
        @Field("otp") otp: Int,
        @Field("passcode") passcode: Int,
    ): Response<OtpVerifyResponse>

    @GET("getVisitList/{id}")
    suspend fun getVisitList(
        @Path("id") id: Int,
        @Query("hash_token") hashToken: String,
        @Query("visit_date") visitDate: String
    ): Response<VisitListResponse>

    @GET("get-all-clients")
    suspend fun getClientsList(
        @Query("hash_token") hashToken: String,
    ): Response<ClientsList>

    @GET("get-client-details/{clientId}")
    suspend fun getClientDetails(
        @Path("clientId") clientId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<ClientDetailsResponse>

    @GET("get-client-careplan-ass/{clientId}")
    suspend fun getClientCarePlanAss(
        @Path("clientId") clientId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<ClientCarePlanAssessment>

    @GET("get-client-careplan-risk-ass")
    suspend fun getClientCarePlanRiskAss(
        @Query("hash_token") hashToken: String,
        @Query("client_id") clientId: Int
    ): Response<CarePlanRiskAssList>

    @FormUrlEncoded
    @POST("addUnscheduledVisits")
    suspend fun addUnscheduledVisits(
        @Query("hash_token") hashToken: String,
        @Field("user_id") userId: String,
        @Field("client_id") clientId: Int,
        @Field("visit_date") visitDate: String,
        @Field("actual_start_time") actualStartTime: String,
        @Field("created_at") createdAt: String,
    ): Response<AddUvVisitResponse>

    @GET("gettododetails/{visitDetailsId}")
    suspend fun getToDoList(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<TodoListResponse>

    @FormUrlEncoded
    @PUT("updatetododetails/{todoId}")
    suspend fun updateTodoDetails(
        @Path("todoId") todoId: Int,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("todo_outcome") todoOutcome: Int,
    ): Response<JsonObject>

    @GET("getUnscheduledTodoDetails/{visitDetailsId}")
    suspend fun getUnscheduledTodoDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<UvTodoListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledTodoDetails")
    suspend fun addUnscheduledTodoDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("todo_user_id") todoUserId: Int,
        @Field("todo_created_at") todoCreatedAt: String,
        @Field("todo_notes") todoNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledTodoDetails/{todoId}")
    suspend fun updateUnscheduledTodoDetails(
        @Path("todoId") todoId: Int,
        @Query("hash_token") hashToken: String,
        @Field("todo_notes") todoNotes: String,
        @Field("todo_user_id") todoUserId: Int,
        @Field("todo_updated_at") todoUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getUnscheduledMedicationDetails/{visitDetailsId}")
    suspend fun getUnscheduledMedicationDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<UvMedicationListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledMedicationDetails")
    suspend fun addUnscheduledMedicationDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("medication_user_id") medicationUserId: Int,
        @Field("medication_created_at") medicationCreatedAt: String,
        @Field("medication_notes") medicationNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledMedicationDetails/{medicationId}")
    suspend fun updateUnscheduledMedicationDetails(
        @Path("medicationId") medicationId: Int,
        @Query("hash_token") hashToken: String,
        @Field("medication_notes") medicationNotes: String,
        @Field("medication_user_id") medicationUserId: Int,
        @Field("medication_updated_at") medicationUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getUnscheduledVisitNotesDetails/{visitDetailsId}")
    suspend fun getUnscheduledVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<UvVisitNotesListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledVisitNotesDetails")
    suspend fun addUnscheduledVisitNotesDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("visit_user_id") visitUserId: Int,
        @Field("visit_created_at") visitCreatedAt: String,
        @Field("visit_notes") visitNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledVisitNotesDetails/{visitNotesId}")
    suspend fun updateUnscheduledVisitNotesDetails(
        @Path("visitNotesId") visitNotesId: Int,
        @Query("hash_token") hashToken: String,
        @Field("visit_notes") visitNotes: String,
        @Field("visit_user_id") visitUserId: Int,
        @Field("visit_updated_at") visitUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getclientvisitnotesdetails/{visitDetailsId}")
    suspend fun getClientVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<ClientVisitNotesDetails>

    @FormUrlEncoded
    @POST("addclientvisitnotesdetails")
    suspend fun addClientVisitNotesDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: Int,
        @Field("updatedby_userid") updatedByUserid: Int,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updatevisitnotesdetails/{visitNotesId}")
    suspend fun updateVisitNotesDetail(
        @Path("visitNotesId") visitNotesId: Int,
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: Int,
        @Field("updatedby_userid") updatedByUserid: Int,
        @Field("updated_at") updatedAt: String,
    ): Response<JsonObject>

    @GET("get-medication-details/{visitDetailsId}")
    suspend fun getMedicationDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<MedicationDetailsListResponse>

    @FormUrlEncoded
    @PUT("medication-blister-pack/{scheduledDetailsId}")
    suspend fun medicationScheduledDetails(
        @Path("scheduledDetailsId") scheduledDetailsId: Int,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("medication-blister-pack/{blisterPackDetailsId}")
    suspend fun medicationBpDetails(
        @Path("blisterPackDetailsId") blisterPackDetailsId: Int,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>
}