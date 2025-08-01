package com.aits.careesteem.view.visits.db_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aits.careesteem.local.converters.StringListConverter
import com.aits.careesteem.view.visits.model.VisitListResponse

@Entity(tableName = "visit_data")
@TypeConverters(StringListConverter::class)
data class VisitEntity(
    var clientId: String?,
    @PrimaryKey var visitDetailsId: String,
    var uatId: Int = 0,
    var clientAddress: String?,
    var clientCity: String?,
    var clientPostcode: String?,
    var clientName: String?,
    var plannedEndTime: String?,
    var plannedStartTime: String?,
    var totalPlannedTime: String?,
    var userId: List<String?> = emptyList(),
    var usersRequired: Int?,
    var latitude: String?,
    var longitude: String?,
    var radius: String?,
    var placeId: String?,
    var visitDate: String?,
    var visitStatus: String?,
    var visitType: String?,
    var actualStartTime: List<String?> = emptyList(),
    var actualEndTime: List<String?> = emptyList(),
    var TotalActualTimeDiff: List<String?> = emptyList(),
    var userName: List<String?> = emptyList(),
    var profile_photo_name: List<String?> = emptyList(),
    var chooseSessions: String?,
    var bufferTime: String?,
    var sessionTime: String?,
    var sessionType: String?,
    var checkInSync: Boolean? = false,
    var checkoutSync: Boolean? = false,
    var visitSync: Boolean? = false
)

// Define a POJO for partial updates
data class VisitUpdateFields(
    @ColumnInfo(name = "visitDetailsId") val id: String,
    @ColumnInfo(name = "visitStatus") val status: String?,
    @ColumnInfo(name = "actualStartTime") val startTime: List<String?>,
    @ColumnInfo(name = "actualEndTime") val endTime: List<String?>,
    @ColumnInfo(name = "TotalActualTimeDiff") val TotalActualTimeDiff: List<String?>
)

fun VisitListResponse.Data.toEntity() = VisitEntity(
    visitDetailsId = visitDetailsId,
    clientId = clientId,
    uatId = uatId,
    clientAddress = clientAddress,
    clientCity = clientCity,
    clientPostcode = clientPostcode,
    clientName = clientName,
    plannedEndTime = plannedEndTime,
    plannedStartTime = plannedStartTime,
    totalPlannedTime = totalPlannedTime,
    userId = userId,
    usersRequired = usersRequired,
    latitude = null,
    longitude = null,
    radius = radius.toString(),
    placeId = placeId,
    visitDate = visitDate,
    visitStatus = visitStatus,
    visitType = visitType,
    actualStartTime = actualStartTime,
    actualEndTime = actualEndTime,
    TotalActualTimeDiff = TotalActualTimeDiff,
    userName = userName,
    profile_photo_name = profile_photo_name,
    chooseSessions = chooseSessions,
    bufferTime = bufferTime,
    sessionTime = sessionTime,
    sessionType = sessionType
)

fun VisitEntity.toVisit(): VisitListResponse.Data {
    return VisitListResponse.Data(
        clientId = clientId ?: "",
        visitDetailsId = visitDetailsId,
        uatId = uatId,
        clientAddress = clientAddress ?: "",
        clientCity = clientCity ?: "",
        clientPostcode = clientPostcode ?: "",
        clientName = clientName ?: "",
        plannedEndTime = plannedEndTime ?: "",
        plannedStartTime = plannedStartTime ?: "",
        totalPlannedTime = totalPlannedTime ?: "",
        userId = userId.filterNotNull(),
        usersRequired = usersRequired ?: 0,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        radius = radius?.toDoubleOrNull() ?: 0.0,
        placeId = placeId ?: "",
        visitDate = visitDate ?: "",
        visitStatus = visitStatus ?: "",
        visitType = visitType ?: "",
        actualStartTime = actualStartTime.filterNotNull(),
        actualEndTime = actualEndTime.filterNotNull(),
        TotalActualTimeDiff = TotalActualTimeDiff.filterNotNull(),
        userName = userName.filterNotNull(),
        profile_photo_name = profile_photo_name.filterNotNull(),
        chooseSessions = chooseSessions ?: "",
        bufferTime = bufferTime ?: "",
        sessionTime = sessionTime ?: "",
        sessionType = sessionType ?: ""
    )
}
