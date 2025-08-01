package com.aits.careesteem.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aits.careesteem.view.visits.db_model.VisitEntity
import com.aits.careesteem.view.visits.db_model.VisitUpdateFields

@Dao
interface VisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<VisitEntity>)

    @Query("SELECT * FROM visit_data")
    suspend fun getAllVisits(): List<VisitEntity>

    @Query("SELECT * FROM visit_data WHERE visitDate = :date")
    suspend fun getVisitsByDate(date: String): List<VisitEntity>

    @Query("DELETE FROM visit_data")
    suspend fun deleteAll()

    @Query("DELETE FROM visit_data WHERE visitDate = :date")
    suspend fun deleteVisitsByDate(date: String)

    // Primary update method using VisitUpdateFields
    @Update(entity = VisitEntity::class)
    suspend fun updateVisitFields(visit: VisitUpdateFields)

    // Bulk update alternative
    @Transaction
    suspend fun updateMultipleVisitFields(visits: List<VisitUpdateFields>) {
        visits.forEach { updateVisitFields(it) }
    }

    @Query("UPDATE visit_data SET actualStartTime = :startTime WHERE visitDetailsId = :visitId")
    suspend fun updateVisitCheckIn(visitId: String, startTime: String)

    @Query("UPDATE visit_data SET actualEndTime = :endTime WHERE visitDetailsId = :visitId")
    suspend fun updateVisitCheckOut(visitId: String, endTime: String)

}
