/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.profile.model

data class UserDetailsResponse(
    val data: List<Data>,
    val message: String,
    val statusCode: Int
) {
    data class Data(
        val Agency: String,
        val address: String,
        val age: Int,
        val city: String,
        val contact_number: String,
        val email: String,
        val name: String,
        val postcode: String,
        //val profile_photo: String,
        val profile_image_url: String,
        val profile_photo_name: String,
        val user_id: String
    )
}