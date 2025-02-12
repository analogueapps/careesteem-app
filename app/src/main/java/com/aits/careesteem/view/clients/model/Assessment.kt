/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.model

data class AssessmentCategory(
    val categoryName: String,
    val items: List<AssessmentItem>
)

data class AssessmentItem(
    val question: String,
    val status: String,
    val comment: String
)

