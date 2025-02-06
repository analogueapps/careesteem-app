/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.model

data class ClientVisitNotesDetails(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val createdAt: String,
        val createdByUserId: Int,
        val createdByUserName: String,
        val id: Int,
        val updatedAt: String,
        val updatedByUserId: Int,
        val updatedByUserName: String,
        val visitDetaiId: Int,
        val visitNotes: String
    )
}