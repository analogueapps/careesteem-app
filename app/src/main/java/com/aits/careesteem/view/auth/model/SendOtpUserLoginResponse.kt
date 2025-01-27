/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.auth.model

data class SendOtpUserLoginResponse(
    val data: UserData,
    val message: String,
    val statusCode: Int
)

data class UserData(
    val admin: Int,
    val contact_number: String,
    val created_at: String,
    val email: String,
    val first_name: String,
    val id: Int,
    val last_name: String,
    val middle_name: String,
    val otp_verified: String,
    val prefix: String,
    val role: String,
    val status: Int,
    val telephone_codes: Int
)