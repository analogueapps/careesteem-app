package com.aits.careesteem.view.unscheduled_visits.model

import com.aits.careesteem.view.visits.model.VisitListResponse

sealed class VisitItem {
    data class VisitCard(val visitData: VisitListResponse.Data) : VisitItem()
    data class TravelTimeIndicator(val timeText: String) : VisitItem()
}
