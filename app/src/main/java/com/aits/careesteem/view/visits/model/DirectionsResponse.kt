/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.model

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline,
    val legs: List<Leg>
)

data class OverviewPolyline(
    val points: String
)

data class Leg(
    val duration: Duration
)

data class Duration(
    val text: String
)
