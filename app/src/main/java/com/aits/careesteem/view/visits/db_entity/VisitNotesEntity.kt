package com.aits.careesteem.view.visits.db_entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_notes")
data class VisitNotesEntity(
    @PrimaryKey val visitNotesId: String,
    val visitDetailsId: String?,
    val visitNotes: String?,
    val createdByUserid: String?,
    val createdByUserName: String?,
    val updatedByUserid: String?,
    val updatedByUserName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val notesSync: Boolean? = false,
)