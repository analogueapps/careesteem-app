/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.model

data class ClientsList(
    val finalData: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val contact_number: String,
        val full_address: String,
        val full_name: String,
        val id: Int,
        val risk_level: String,
        val profile_photo_name: String,
        val profile_photo: String,
        val created_at: String
    )
}