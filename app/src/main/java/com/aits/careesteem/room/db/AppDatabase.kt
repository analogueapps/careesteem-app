package com.aits.careesteem.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aits.careesteem.local.converters.Converters
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.db_entity.VisitNotesEntity

@Database(
    entities = [VisitEntity::class, MedicationEntity::class, TodoEntity::class, AutoAlertEntity::class, VisitNotesEntity::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
}