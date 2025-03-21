package com.aits.careesteem.view.notification.model

data class NotificationListResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val client_id: Int,
        val created_at: String,
        val id: Int,
        val notification_body: String,
        val notification_title: String,
        val updated_at: String,
        val user_id: Int,
        val visit_details_id: Int
    )
}