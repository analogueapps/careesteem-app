/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.model

data class VisitListResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val visitDetailsId: Int,
        val clientAddress: String,
        val clientName: String,
        val plannedEndTime: String,
        val plannedStartTime: String,
        val totalPlannedTime: String,
        val userId: Int,
        val usersRequired: Any,
        val latitude: Any,
        val longitude: Any,
        val radius: Any,
        val placeId: Any,
        val visitDate: String,
        val visitStatus: String
    )
}