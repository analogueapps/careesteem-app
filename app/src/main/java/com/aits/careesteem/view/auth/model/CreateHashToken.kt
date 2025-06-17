package com.aits.careesteem.view.auth.model

data class CreateHashToken(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val admin: Int,
        val contact_number: String,
        val created_at: String,
        val email: String,
        val first_name: String,
        val hash_token: String,
        val id: String,
        val last_name: String,
        val middle_name: String,
        val prefix: String,
        val role: String,
        val status: Int,
        val telephone_codes: String
    )
}