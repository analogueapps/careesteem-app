/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.model

import java.io.Serializable

data class MedicationDetailsListResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val blister_pack_created_by: String,
        val blister_pack_date: String,
        val blister_pack_details_id: String,
        val blister_pack_end_date: String,
        val blister_pack_id: String,
        val blister_pack_start_date: String,
        val blister_pack_user_id: String,
        val by_exact_date: String,
        val by_exact_end_date: String,
        val by_exact_start_date: String,
        val client_id: String,
        val day_name: String,
        val medication_id: String,
        val medication_route_name: String,
        val medication_support: String,
        val medication_type: String,
        val nhs_medicine_name: String,
        val quantity_each_dose: String,
        val scheduled_created_by: String,
        val scheduled_date: String,
        val scheduled_details_id: String,
        val prn_details_id: String,
        val scheduled_end_date: String,
        val scheduled_id: String,
        val scheduled_start_date: String,
        val scheduled_user_id: String,
        val select_preference: String,
        val by_exact_time: String,
        val session_type: String,
        val visit_details_id: Any,
        // Newly added fields
        val prn_id: String,
        val prn_start_date: String,
        val prn_end_date: String,
        val dose_per: Int,
        val doses: Int,
        val time_frame: String,
        val prn_offered: String,
        val prn_be_given: String,
        val prn_user_id: String,
        val status: String,
        val prn_details_status: String,
        val prn_created_by: String,
        // Body map
        val body_map_image_url: List<String>,
        val body_part_names: List<String>,

        val carer_notes: String,
        val additional_instructions: String
        // want to change body_map_image_url instead of body_image
    ) : Serializable
}