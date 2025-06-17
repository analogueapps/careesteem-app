package com.aits.careesteem.view.alerts.model

data class ClientNameListResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val clientId: String,
        val clientName: String,
        val visitDate: String,
        val visitDetailsId: String
    )
}