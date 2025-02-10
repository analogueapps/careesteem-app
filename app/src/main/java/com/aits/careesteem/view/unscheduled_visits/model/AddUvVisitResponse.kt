/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.unscheduled_visits.model

data class AddUvVisitResponse(
    val message: String,
    val statusCode: Int,
    val userActualTimeData: List<UserActualTimeData>
) {
    data class UserActualTimeData(
        val actual_end_time: Any,
        val actual_start_time: String,
        val client_id: Int,
        val created_at: String,
        val id: Int,
        val status: Any,
        val updated_at: Any,
        val user_id: Int,
        val visit_details_id: Int
    )
}