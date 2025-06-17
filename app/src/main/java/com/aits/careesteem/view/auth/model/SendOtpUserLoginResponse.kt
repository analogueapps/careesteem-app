/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.model

data class SendOtpUserLoginResponse(
    val data: Data,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val id: String,
        val prefix: String,
        val first_name: String,
        val middle_name: String,
        val last_name: String,
        val contact_number: String,
        val email: String,
        val admin: Int,
        val role: String,
        val created_at: String,
        val token: String,
        val hash_token: String,
        val status: Int,
        val telephone_codes: Int,
        val token_status: String,
        val updated_at: String,
        val allocated: String,
        val otp: Int,
        val otp_expires_at: String,
        val otp_verified: Any,
        val passcode: String
    )
}