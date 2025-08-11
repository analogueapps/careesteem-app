package com.aits.careesteem.view.visits.db_entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val todoDetailsId: String,
    val visitDetailsId: String?,
    val additionalNotes: String?,
    val carerNotes: String?,
    val todoEssential: Boolean?,
    val todoName: String?,
    val todoOutcome: String?,
    val todoSync: Boolean? = false,
)
