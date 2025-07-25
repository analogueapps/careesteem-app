package com.aits.careesteem.view.visits.model

data class VisitDetailsResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val TotalActualTimeDiff: List<String>,
        val actualEndTime: List<String>,
        val actualStartTime: List<String>,
        val chooseSessions: Any,
        val clientAddress: String,
        val clientCity: String,
        val clientPostcode: String,
        val clientId: String,
        val clientName: String,
        val bufferTime: String,
        val latitude: Any,
        val longitude: Any,
        val placeId: String,
        val plannedEndTime: String,
        val plannedStartTime: String,
        val profile_photo: List<String>,
        val profile_photo_name: List<String>,
        val client_profile_image_url: String,
        val radius: Any,
        val sessionTime: String,
        val sessionType: String,
        val totalPlannedTime: String,
        val uatId: Int,
        val userId: List<String>,
        val userName: List<String>,
        val usersRequired: Any,
        val visitDate: String,
        val visitDetailsId: String,
        val visitStatus: String,
        val visitType: String
    )
}