/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.view.alerts.model.AlertListResponse
import com.aits.careesteem.view.alerts.model.ClientNameListResponse
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
        @Field("fcm_token") fcmToken: String,
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

    @GET("get-visit-details/{visitDetailsId}/{userId}")
    suspend fun getVisitDetails(
        @Path("visitDetailsId") visitDetailsId: Int,
        @Path("userId") userId: Int,
        @Query("hash_token") hashToken: String
    ): Response<VisitDetailsResponse>

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
    @PUT("medication-scheduled/{scheduledDetailsId}")
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

    @FormUrlEncoded
    @POST("medication-prn-details")
    suspend fun medicationPrnDetails(
        @Query("hash_token") hashToken: String,
        @Field("client_id") clientId: Int,
        @Field("medication_id") medicationId: Int,
        @Field("prn_id") prnId: Int,
        @Field("dose_per") doesPer: Int,
        @Field("doses") doses: Int,
        @Field("time_frame") timeFrame: String,
        @Field("prn_offered") prnOffered: String,
        @Field("prn_be_given") prnBeGiven: String,
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("user_id") userId: Int,
        @Field("medication_time") medicationTime: String,
        @Field("created_at") createdAt: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>

    @GET("get-all-users/{userId}")
    suspend fun getUserDetailsById(
        @Path("userId") userId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<UserDetailsResponse>

    @FormUrlEncoded
    @POST("add-Visit-Checkin")
    suspend fun addVisitCheckIn(
        @Query("hash_token") hashToken: String,
        @Field("client_id") clientId: Int,
        @Field("visit_details_id") visitDetailsId: Int,
        @Field("user_id") userId: Int,
        @Field("status") status: String,
        @Field("actual_start_time") actualStartTime: String,
        @Field("created_at") createdAt: String,
    ): Response<AddVisitCheckInResponse>

    @FormUrlEncoded
    @PUT("update-visit-checkout/{userId}/{visitDetailsId}/")
    suspend fun updateVisitCheckout(
        @Path("userId") userId: Int,
        @Path("visitDetailsId") visitDetailsId: Int,
        @Query("hash_token") hashToken: String,
        @Field("actual_end_time") actualEndTime: String,
        @Field("status") status: String,
        @Field("updated_at") updatedAt: String,
    ): Response<UpdateVisitCheckoutResponse>

    @FormUrlEncoded
    @POST("verify-qrcode/{clientId}")
    suspend fun verifyQrCode(
        @Path("clientId") clientId: Int,
        @Query("hash_token") hashToken: String,
        @Field("qrcode_token") qrcodeToken: String
    ): Response<JsonObject>

    @GET("getClientNameList/{userId}")
    suspend fun getClientsList(
        @Path("userId") userId: Int,
        @Query("hash_token") hashToken: String,
        @Query("visit_date") visitDate: String
    ): Response<ClientNameListResponse>

    @Multipart
    @POST("alert")
    suspend fun sendAlert(
        @Query("hash_token") hashToken: String,
        @Part("client_id") clientId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("visit_details_id") visitDetailsId: RequestBody,
        @Part("severity_of_concern") severityOfConcern: RequestBody,
        @Part("concern_details") concernDetails: RequestBody,
        @Part("body_part_type") bodyPartType: RequestBody,
        @Part("body_part_names") bodyPartNames: RequestBody,
        @Part("file_name") fileName: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("created_at") createdAt: RequestBody,
    ): Response<JsonObject>

    @GET("alert-get-list/{userId}")
    suspend fun getAlertsList(
        @Path("userId") userId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<AlertListResponse>


    @GET("get-all-notifications/{userId}")
    suspend fun getNotificationList(
        @Path("userId") userId: Int,
        @Query("hash_token") hashToken: String,
    ): Response<NotificationListResponse>
}