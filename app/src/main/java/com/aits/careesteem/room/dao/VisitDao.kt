package com.aits.careesteem.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity

@Dao
interface VisitDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(meds: List<MedicationEntity>)

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
            checkInSync = :checkInSync
        WHERE visitDetailsId = :visitDetailsId
    """)
    suspend fun updateVisitCheckInTimesAndSync(
        visitDetailsId: String,
        actualStartTime: String,
        actualStartTimeString: String?,
        checkInSync: Boolean
    )

    @Query("""
        UPDATE visits
        SET 
            actualEndTime = :actualEndTime,
            actualEndTimeString = :actualEndTimeString,
            checkOutSync = :checkOutSync
        WHERE visitDetailsId = :visitDetailsId
    """)
    suspend fun updateVisitCheckOutTimesAndSync(
        visitDetailsId: String,
        actualEndTime: String,
        actualEndTimeString: String?,
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

    @Query("SELECT * FROM medications WHERE visitDetailsId = :visitId")
    suspend fun getMedicationListByVisitsDetailsId(visitId: String): List<MedicationEntity>

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


}
