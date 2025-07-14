/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.unscheduled_visits.model

data class UvVisitNotesListResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val id: String,
        val visit_created_at: String,
        val user_last_name: String,
        val created_at: String,
        val updated_at: String,
        val visit_details_id: String,
        val visit_notes: String,
        val visit_updated_at: String,
        val visit_user_id: String
    )
}