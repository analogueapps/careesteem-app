package com.aits.careesteem.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.db_entity.VisitNotesEntity

@Dao
interface VisitDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    // clear medication table
    @Query("DELETE FROM medications")
    suspend fun clearMedications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(meds: List<MedicationEntity>)

    // single medication
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)

    @Query("SELECT * FROM visits")
    suspend fun getAllVisits(): List<VisitEntity>

    @Query("SELECT * FROM visits WHERE visitDate = :visitDate")
    suspend fun getAllVisitsByDate(visitDate: String): List<VisitEntity>

    @Query("SELECT * FROM visits WHERE visitDetailsId = :visitId")
    suspend fun getVisitsDetailsById(visitId: String): VisitEntity

    @Query("SELECT * FROM medications WHERE visitDetailsId = :visitId")
    suspend fun getMedicationsForVisit(visitId: String): List<MedicationEntity>

    @Query("SELECT * FROM todos WHERE visitDetailsId = :visitId")
    suspend fun getTodosForVisit(visitId: String): List<TodoEntity>

    @Query("SELECT uatId FROM visits WHERE visitDetailsId = :visitId")
    suspend fun getUatId(visitId: String): String

    @Query("""
        UPDATE visits
        SET 
            actualStartTime = :actualStartTime,
            actualStartTimeString = :actualStartTimeString,
            visitStatus = :visitStatus,
            checkInSync = :checkInSync
        WHERE visitDetailsId = :visitDetailsId
    """)
    suspend fun updateVisitCheckInTimesAndSync(
        visitDetailsId: String,
        actualStartTime: String,
        actualStartTimeString: String?,
        visitStatus: String?,
        checkInSync: Boolean
    )

    @Query("SELECT actualStartTimeString FROM visits WHERE visitDetailsId = :visitId")
    suspend fun getActualStartTimeString(visitId: String): String

    @Query("""
        UPDATE visits
        SET 
            actualEndTime = :actualEndTime,
            actualEndTimeString = :actualEndTimeString,
            TotalActualTimeDiff = :totalActualTimeDiff,
            visitStatus = :visitStatus,
            checkOutSync = :checkOutSync
        WHERE visitDetailsId = :visitDetailsId
    """)
    suspend fun updateVisitCheckOutTimesAndSync(
        visitDetailsId: String,
        actualEndTime: String,
        actualEndTimeString: String?,
        totalActualTimeDiff: String?,
        visitStatus: String?,
        checkOutSync: Boolean
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoAlerts(autoAlerts: AutoAlertEntity)

    @Query("SELECT * FROM todos WHERE visitDetailsId = :visitId")
    suspend fun getTodoListByVisitsDetailsId(visitId: String): List<TodoEntity>

    @Query("""
        UPDATE todos
        SET 
            carerNotes = :carerNotes,
            todoOutcome = :todoOutcome,
            todoSync = :todoSync
        WHERE todoDetailsId = :todoDetailsId
    """)
    suspend fun updateTodoListById(
        todoDetailsId: String,
        carerNotes: String,
        todoOutcome: Boolean,
        todoSync: Boolean
    )

//    @Query("SELECT * FROM medications WHERE visitDetailsId = :visitId")
//    suspend fun getMedicationListByVisitsDetailsId(visitId: String): List<MedicationEntity>

    @Query("""
    SELECT * FROM medications
    WHERE visitDetailsId = :visitId
       OR visitDetailsId LIKE :visitId || ',%'
       OR visitDetailsId LIKE '%,' || :visitId
       OR visitDetailsId LIKE '%,' || :visitId || ',%'
    """)
    suspend fun getMedicationListByVisitsDetailsId(
        visitId: String
    ): List<MedicationEntity>


    @Query("""
        UPDATE medications
        SET 
            status = :status,
            carer_notes = :carerNotes,
            medicationSync = :medicationSync,
            medicationBlisterPack = :medicationBlisterPack
        WHERE blister_pack_details_id = :blisterPackDetailsId
    """)
    suspend fun updateMedicationByBlisterPackDetailsId(
        blisterPackDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationBlisterPack: Boolean
    )

    @Query("""
        UPDATE medications
        SET 
            status = :status,
            carer_notes = :carerNotes,
            medicationSync = :medicationSync,
            medicationScheduled = :medicationScheduled
        WHERE scheduled_details_id = :scheduledDetailsId
    """)
    suspend fun updateMedicationByScheduledDetailsId(
        scheduledDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationScheduled: Boolean
    )

    @Query("""
        UPDATE medications
        SET 
            status = :status,
            carer_notes = :carerNotes,
            medicationSync = :medicationSync,
            medicationPrnUpdate = :medicationPrnUpdate
        WHERE prn_details_id = :prnDetailsId
    """)
    suspend fun updateMedicationByPrnDetailsId(
        prnDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationPrnUpdate: Boolean
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitNotes(visitNotes: VisitNotesEntity)

    @Query("SELECT * FROM visit_notes WHERE visitDetailsId = :visitDetailsId")
    suspend fun getAllVisitNotesByVisitDetailsId(visitDetailsId: String): List<VisitNotesEntity>

    @Query("""
        UPDATE visit_notes
        SET 
            visitNotes = :visitNotes,
            updatedByUserid = :updatedByUserid,
            updatedByUserName = :updatedByUserName,
            updatedAt = :updatedAt
        WHERE visitNotesId = :visitNotesId
    """)
    suspend fun updateVisitNotesById(
        visitNotesId: String,
        visitNotes: String,
        updatedByUserid: String,
        updatedByUserName: String,
        updatedAt: String
    )

    @Query("""
        SELECT * 
        FROM todos 
        WHERE visitDetailsId = :visitDetailsId
          AND todoEssential = 1
          AND (todoOutcome IS NULL OR todoOutcome = '')
    """)
    fun getTodosWithEssentialAndEmptyOutcome(visitDetailsId: String): List<TodoEntity>

    @Query("""
        SELECT * 
        FROM medications
        WHERE visitDetailsId = :visitDetailsId
          AND medication_type != 'PRN'
          AND status = 'Scheduled'
    """)
    fun getMedicationsWithScheduled(visitDetailsId: String): List<MedicationEntity>
}
