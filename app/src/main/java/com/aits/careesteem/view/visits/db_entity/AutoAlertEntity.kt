package com.aits.careesteem.view.visits.db_entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auto_alert")
data class AutoAlertEntity(
    @PrimaryKey val colId: String,
    val visitDetailsId: String?,
    val clientId: String?,
    val uatId: String?,
    val userId: String?,
    val alertType: String?,
    val alertStatus: String?,
    val createdAt: String?,
    val alertAction: Int?, // (1. Visit, 2. Todo, 3. Medication)
    val todoDetailsId: String?,
    val scheduledId: String?,
    val blisterPackId: String?,
    val alertSync: Boolean? = false,
)
