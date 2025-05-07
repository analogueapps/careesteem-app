package com.aits.careesteem.view.alerts.model

data class AlertListResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val body_image: List<String>,
        val body_part_names: List<String>,
        val body_part_type: List<String>,
        val choose_sessions: Any,
        val client_id: Int,
        val client_name: String,
        val concern_details: String,
        val created_at: String,
        val id: Int,
        val session_time: String,
        val session_type: String,
        val severity_of_concern: String,
        val user_id: Int,
        val user_name: String,
        val visit_details_id: Int
    )
}