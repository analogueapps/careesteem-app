package com.aits.careesteem.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aits.careesteem.local.converters.StringListConverter
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.view.visits.db_model.VisitEntity

@Database(entities = [VisitEntity::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
}
