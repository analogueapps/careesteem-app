package com.aits.careesteem.view.visits.db_entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val visitDetailsId: String,
    val agencyId: String?,
    val bufferTime: String?,
    val chooseSessions: String?,
    val clientAddress: String?,
    val clientCity: String?,
    val clientId: String?,
    val clientName: String?,
    val clientPostcode: String?,
    val clientProfileImageUrl: String?,
    val gioStatus: String?,
    val placeId: String?,
    val plannedEndTime: String?,
    val plannedStartTime: String?,
    val profilePhotoName: String?,
    val radius: Int?,
    val sessionTime: String?,
    val sessionType: String?,
    val totalPlannedTime: String?,
    val usersRequired: Int?,
    val visitDate: String?,
    val visitStatus: String?,
    val visitType: String?,
    val userId: String?,
    val userName: String?,
    var uatId: String?,
    var actualStartTime: String?,
    var actualEndTime: String?,
    var TotalActualTimeDiff: String?,
    var actualStartTimeString: String?,
    var actualEndTimeString: String?,
    var checkInSync: Boolean = false,
    var checkOutSync: Boolean = false,
    var medicationSync: Boolean = false,
    var todoSync: Boolean = false,
    var visitSync: Boolean = false,
)
