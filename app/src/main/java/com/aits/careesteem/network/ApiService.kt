/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
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
        @Field("telephone_codes") telephoneCodes: String,
    ): Response<SendOtpUserLoginResponse>

    @FormUrlEncoded
    @POST("verify-otp")
    suspend fun verifyOtp(
        @Field("contact_number") contactNumber: String,
        @Field("country_code") countryCode: String,
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
    @POST("select-dbname")
    suspend fun selectDbName(
        @Field("agency_id") agencyId: String,
        @Field("contact_number") contactNumber: String,
        @Field("user_id") userId: String
    ): Response<CreateHashToken>

    @FormUrlEncoded
    @POST("create-passcode")
    suspend fun createPasscode(
        @Query("hash_token") hashToken: String,
        @Field("contact_number") contactNumber: String,
        @Field("passcode") passcode: Int,
    ): Response<JsonObject>

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

    @GET("getVisitList/{userId}")
    suspend fun getVisitList(
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String,
        @Query("visit_date") visitDate: String
    ): Response<VisitListResponse>

    @GET("get-visit-details/{visitDetailsId}/{userId}")
    suspend fun getVisitDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String
    ): Response<VisitDetailsResponse>

    @GET("get-all-clients")
    suspend fun getClientsList(
        @Query("hash_token") hashToken: String,
    ): Response<ClientsList>

    @GET("get-client-details/{clientId}")
    suspend fun getClientDetails(
        @Path("clientId") clientId: String,
        @Query("hash_token") hashToken: String,
    ): Response<ClientDetailsResponse>

    @GET("get-client-careplan-ass/{clientId}")
    suspend fun getClientCarePlanAss(
        @Path("clientId") clientId: String,
        @Query("hash_token") hashToken: String,
    ): Response<ClientCarePlanAssessment>

    @GET("get-client-careplan-risk-ass")
    suspend fun getClientCarePlanRiskAss(
        @Query("hash_token") hashToken: String,
        @Query("client_id") clientId: String
    ): Response<CarePlanRiskAssList>

    @GET("get-uploaded-documents")
    suspend fun getUploadedDocuments(
        @Query("hash_token") hashToken: String,
        @Query("client_id") clientId: String
    ): Response<UploadedDocumentsResponse>

    @FormUrlEncoded
    @POST("addUnscheduledVisits")
    suspend fun addUnscheduledVisits(
        @Query("hash_token") hashToken: String,
        @Field("user_id") userId: String,
        @Field("client_id") clientId: String,
        @Field("visit_date") visitDate: String,
        @Field("actual_start_time") actualStartTime: String,
        @Field("created_at") createdAt: String,
    ): Response<AddUvVisitResponse>

    @GET("gettododetails/{visitDetailsId}")
    suspend fun getToDoList(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<TodoListResponse>

    @FormUrlEncoded
    @PUT("updatetododetails/{todoId}")
    suspend fun updateTodoDetails(
        @Path("todoId") todoId: String,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("todo_outcome") todoOutcome: Int,
    ): Response<JsonObject>

    @GET("getUnscheduledTodoDetails/{visitDetailsId}")
    suspend fun getUnscheduledTodoDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<UvTodoListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledTodoDetails")
    suspend fun addUnscheduledTodoDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("todo_user_id") todoUserId: String,
        @Field("todo_created_at") todoCreatedAt: String,
        @Field("todo_notes") todoNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledTodoDetails/{todoId}")
    suspend fun updateUnscheduledTodoDetails(
        @Path("todoId") todoId: String,
        @Query("hash_token") hashToken: String,
        @Field("todo_notes") todoNotes: String,
        @Field("todo_user_id") todoUserId: String,
        @Field("todo_updated_at") todoUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getUnscheduledMedicationDetails/{visitDetailsId}")
    suspend fun getUnscheduledMedicationDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<UvMedicationListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledMedicationDetails")
    suspend fun addUnscheduledMedicationDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("medication_user_id") medicationUserId: String,
        @Field("medication_created_at") medicationCreatedAt: String,
        @Field("medication_notes") medicationNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledMedicationDetails/{medicationId}")
    suspend fun updateUnscheduledMedicationDetails(
        @Path("medicationId") medicationId: String,
        @Query("hash_token") hashToken: String,
        @Field("medication_notes") medicationNotes: String,
        @Field("medication_user_id") medicationUserId: String,
        @Field("medication_updated_at") medicationUpdatedAt: String,
    ): Response<JsonObject>

    @GET("getUnscheduledVisitNotesDetails/{visitDetailsId}")
    suspend fun getUnscheduledVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<UvVisitNotesListResponse>

    @FormUrlEncoded
    @POST("addUnscheduledVisitNotesDetails")
    suspend fun addUnscheduledVisitNotesDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_user_id") visitUserId: String,
        @Field("visit_created_at") visitCreatedAt: String,
        @Field("createdby_userid") createdByUserId: String,
        @Field("visit_notes") visitNotes: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updateUnscheduledVisitNotesDetails/{visitNotesId}")
    suspend fun updateUnscheduledVisitNotesDetails(
        @Path("visitNotesId") visitNotesId: String,
        @Query("hash_token") hashToken: String,
        @Field("visit_notes") visitNotes: String,
        @Field("updated_at") updatedAt: String,
        @Field("updatedby_userid") updatedByUserid: String,
    ): Response<JsonObject>

    @GET("getclientvisitnotesdetails/{visitDetailsId}")
    suspend fun getClientVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<ClientVisitNotesDetails>

    @FormUrlEncoded
    @POST("addclientvisitnotesdetails")
    suspend fun addClientVisitNotesDetails(
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: String,
        @Field("updatedby_userid") updatedByUserid: String,
        @Field("created_at") createdAt: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("updatevisitnotesdetails/{visitNotesId}")
    suspend fun updateVisitNotesDetail(
        @Path("visitNotesId") visitNotesId: String,
        @Query("hash_token") hashToken: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("visit_notes") visitNotes: String,
        @Field("createdby_userid") createdByUserid: String,
        @Field("updatedby_userid") updatedByUserid: String,
        @Field("updated_at") updatedAt: String,
    ): Response<JsonObject>

    @GET("get-medication-details/{visitDetailsId}")
    suspend fun getMedicationDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
    ): Response<MedicationDetailsListResponse>

    @GET("get-unscheduled-medication-prn")
    suspend fun getUnscheduledMedicationPrn(
        @Query("hash_token") hashToken: String,
        @Query("client_id") clientId: String,
        @Query("date") date: String
    ): Response<MedicationDetailsListResponse>

    @FormUrlEncoded
    @PUT("medication-scheduled/{scheduledDetailsId}")
    suspend fun medicationScheduledDetails(
        @Path("scheduledDetailsId") scheduledDetailsId: String,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
        @Field("scheduled_outcome") scheduledOutcome: Int,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("medication-blister-pack/{blisterPackDetailsId}")
    suspend fun medicationBpDetails(
        @Path("blisterPackDetailsId") blisterPackDetailsId: String,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
        @Field("blister_pack_outcome") blisterPackOutcome: Int,
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("medication-prn-details/{prnDetailsId}")
    suspend fun updateMedicationPrn(
        @Path("prnDetailsId") prnDetailsId: String,
        @Query("hash_token") hashToken: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("medication-prn-details")
    suspend fun medicationPrnDetails(
        @Query("hash_token") hashToken: String,
        @Field("client_id") clientId: String,
        @Field("medication_id") medicationId: String,
        @Field("prn_id") prnId: String,
        @Field("dose_per") doesPer: Int,
        @Field("doses") doses: Int,
        @Field("time_frame") timeFrame: String,
        @Field("prn_offered") prnOffered: String,
        @Field("prn_be_given") prnBeGiven: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("user_id") userId: String,
        @Field("medication_time") medicationTime: String,
        @Field("created_at") createdAt: String,
        @Field("carer_notes") carerNotes: String,
        @Field("status") status: String,
    ): Response<JsonObject>

    @GET("get-all-users/{userId}")
    suspend fun getUserDetailsById(
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String,
    ): Response<UserDetailsResponse>

    @FormUrlEncoded
    @POST("add-Visit-Checkin")
    suspend fun addVisitCheckIn(
        @Query("hash_token") hashToken: String,
        @Field("client_id") clientId: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("user_id") userId: String,
        @Field("status") status: String,
        @Field("actual_start_time") actualStartTime: String,
        @Field("created_at") createdAt: String,
    ): Response<AddVisitCheckInResponse>

    @GET("checkin-validation/{userId}/")
    suspend fun checkInEligible(
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String
    ): Response<JsonObject>

    @GET("get-todo-essential-details/{visitDetailsId}/")
    suspend fun checkOutEligible(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @PUT("update-visit-checkout/{userId}/{visitDetailsId}/")
    suspend fun updateVisitCheckout(
        @Path("userId") userId: String,
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String,
        @Field("actual_end_time") actualEndTime: String,
        @Field("status") status: String,
        @Field("updated_at") updatedAt: String,
    ): Response<UpdateVisitCheckoutResponse>

    @FormUrlEncoded
    @POST("add-Alert-Check-In-Out")
    suspend fun automaticAlerts(
        @Query("hash_token") hashToken: String,
        @Field("uat_id") uatId: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("client_id") clientId: String,
        @Field("user_id") userId: String,
        @Field("alert_type") alertType: String,
        @Field("alert_status") alertStatus: String,
        @Field("created_at") createdAt: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("add-Todo-Alert-Details")
    suspend fun automaticTodoAlerts(
        @Query("hash_token") hashToken: String,
        @Field("todo_details_id") todoDetailsId: String,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("client_id") clientId: String,
        @Field("alert_type") alertType: String,
        @Field("alert_status") alertStatus: String,
        @Field("created_at") createdAt: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("add-Medication-Alert-Details")
    suspend fun automaticMedicationAlerts(
        @Query("hash_token") hashToken: String,
        @Field("scheduled_id") scheduledId: Any,
        @Field("blister_pack_id") blisterPackId: Any,
        @Field("visit_details_id") visitDetailsId: String,
        @Field("client_id") clientId: String,
        @Field("alert_type") alertType: String,
        @Field("alert_status") alertStatus: String,
        @Field("created_at") createdAt: String,
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("verify-qrcode/{clientId}")
    suspend fun verifyQrCode(
        @Path("clientId") clientId: String,
        @Query("hash_token") hashToken: String,
        @Field("qrcode_token") qrcodeToken: String
    ): Response<JsonObject>

    @GET("getClientNameList/{userId}")
    suspend fun getClientsList(
        @Path("userId") userId: String,
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
        @Part("alerts_status") alertsStatus: RequestBody,
    ): Response<JsonObject>

    @GET("alert-get-list/{userId}")
    suspend fun getAlertsList(
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String,
    ): Response<AlertListResponse>


    @GET("get-all-notifications/{userId}")
    suspend fun getNotificationList(
        @Path("userId") userId: String,
        @Query("hash_token") hashToken: String,
    ): Response<NotificationListResponse>

    @PUT("clear-notification")
    suspend fun clearNotification(
        @Query("hash_token") hashToken: String,
        @Body request: ClearNotificationRequest
    ): Response<JsonObject>

    @GET("get-client-Previous-visitnotes-details/{visitDetailsId}")
    suspend fun getClientPreviousVisitNotesDetails(
        @Path("visitDetailsId") visitDetailsId: String,
        @Query("hash_token") hashToken: String
    ): Response<ClientVisitNotesDetails>
}