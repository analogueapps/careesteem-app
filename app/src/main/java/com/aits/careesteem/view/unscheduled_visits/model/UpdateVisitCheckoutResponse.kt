package com.aits.careesteem.view.unscheduled_visits.model

data class UpdateVisitCheckoutResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val actual_end_time: String,
        val actual_start_time: String,
        val client_id: String,
        val created_at: String,
        val id: String,
        val status: String,
        val updated_at: String,
        val user_id: String,
        val visit_details_id: String
    )
}