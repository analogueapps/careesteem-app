/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.model

data class ClientDetailsResponse(
    val data: Data,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val About: AboutData,
        val MyCareNetwork: List<MyCareNetworkData>
    ) {
        data class AboutData(
            val age: String,
            val client_id: String,
            val client_personal_id: String,
            val date_of_birth: String,
            val ethnicity: String,
            val gender: String,
            val marital_status: String,
            val nhs_number: String,
            val religion: String
        )

        data class MyCareNetworkData(
            val address: String,
            val age: String,
            val city: String,
            val contact_number: String,
            val email: String,
            val mycare_network_id: String,
            val name: String,
            val occupation_type: String,
            val post_code: String
        )
    }
}