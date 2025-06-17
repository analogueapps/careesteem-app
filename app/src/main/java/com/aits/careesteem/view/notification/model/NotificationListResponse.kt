package com.aits.careesteem.view.notification.model

data class NotificationListResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val client_id: String,
        val created_at: String,
        val id: String,
        val notification_body: String,
        val notification_title: String,
        val updated_at: String,
        val user_id: String,
        val visit_details_id: String
    )
}