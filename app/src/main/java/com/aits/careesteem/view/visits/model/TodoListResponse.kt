/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.model

import java.io.Serializable

data class TodoListResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val additionalNotes: String,
        val carerNotes: String,
        val todoDetailsId: String,
        val todoName: String,
        val todoEssential: Boolean,
        val todoOutcome: String
    ) : Serializable
}