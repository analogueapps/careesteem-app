/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.unscheduled_visits.model

data class UvTodoListResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val id: String,
        val todo_created_at: String,
        val todo_notes: String,
        val todo_updated_at: String,
        val todo_user_id: String,
        val visit_details_id: String
    )
}