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
        var clientId: Int,
        var visitDetailsId: Int,
        var uatId: Int = 0,
        var clientAddress: String,
        var clientName: String,
        var plannedEndTime: String,
        var plannedStartTime: String,
        var totalPlannedTime: String,
        var userId: String,
        var usersRequired: Int,
        var latitude: Any,
        var longitude: Any,
        var radius: Any,
        var placeId: String,
        var visitDate: String,
        var visitStatus: String,
        var visitType: String,
        var actualStartTime: List<String> = emptyList(),
        var actualEndTime: List<String> = emptyList(),
        var TotalActualTimeDiff: List<String> = emptyList(),
        var userName: List<String> = emptyList(),
        var profile_photo: List<String> = emptyList()
    )
}

data class User(
    val name: String,
    val photoUrl: String
)
