package com.aits.careesteem.view.clients.model

data class UploadedDocumentsResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val additional_info: String,
        val agency_id: String,
        val attach_document: List<AttachDocument>,
        val client_id: String,
        val created_at: String,
        val document_name: String,
        val id: String,
        val updated_at: String
    ) {
        data class AttachDocument(
            val filename: String,
            val url: String
        )
    }
}