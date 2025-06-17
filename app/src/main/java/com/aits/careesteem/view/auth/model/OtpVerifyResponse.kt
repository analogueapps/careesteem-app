/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.model

data class OtpVerifyResponse(
    val data: List<Data>,
    val dbList: List<DbList>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val admin: Int,
        val allocated: Any,
        val contact_number: String,
        val created_at: String,
        val email: String,
        val first_name: String,
        val id: String,
        val last_name: String,
        val middle_name: String,
        val otp: Int,
        val otp_expires_at: String,
        val otp_verified: Boolean,
        val passcode: String,
        val prefix: String,
        val role: String,
        val status: Int,
        val telephone_codes: String,
        val token: Any,
        val hash_token: Any,
        val token_status: Any,
        val updated_at: String
    )

    data class DbList(
        val id: String,
        val contact_number: String,
        val user_db_name: String,
        val agency_id: String,
        val agency_name: String
    )
}