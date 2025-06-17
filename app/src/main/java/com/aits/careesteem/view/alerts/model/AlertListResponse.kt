package com.aits.careesteem.view.alerts.model

data class AlertListResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        //val body_image: List<String>,
        val body_map_image_url: List<String>,
        val body_part_names: List<String>,
        val body_part_type: List<String>,
        val choose_sessions: Any,
        val client_id: String,
        val client_name: String,
        val concern_details: String,
        val created_at: String,
        val id: String,
        val session_time: String,
        val session_type: String,
        val severity_of_concern: String,
        val user_id: String,
        val user_name: String,
        val visit_details_id: String
    )
}