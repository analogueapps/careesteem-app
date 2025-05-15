/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import com.aits.careesteem.view.visits.model.PlaceDetailsResponse
import com.aits.careesteem.view.visits.model.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface GoogleApiService {
    @GET("maps/api/place/details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<PlaceDetailsResponse>

    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("mode") mode: String,
    ): Call<DirectionsResponse>

    @GET("maps/api/directions/json")
    fun getTravelTime(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionsResponse>
}
