package com.aits.careesteem.room.repo

import androidx.room.Query
import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.db_entity.VisitNotesEntity
import javax.inject.Inject

class VisitRepository @Inject constructor(
    private val visitDao: VisitDao
) {

    suspend fun clearAllTables() {
        visitDao.clearVisits()
        visitDao.clearMedications()
        visitDao.clearTodos()
        visitDao.clearVisitNotes()
    }

    suspend fun insertVisit(visit: VisitEntity) = visitDao.insertVisit(visit)
    suspend fun insertMedications(meds: List<MedicationEntity>) = visitDao.insertMedications(meds)
    suspend fun insertMedication(meds: MedicationEntity) = visitDao.insertMedication(meds)

    suspend fun insertTodos(todos: List<TodoEntity>) = visitDao.insertTodos(todos)

    suspend fun getAllVisits(): List<VisitEntity> = visitDao.getAllVisits()

    suspend fun getAllVisitsByDate(visitDate: String): List<VisitEntity> = visitDao.getAllVisitsByDate(visitDate = visitDate)

    suspend fun getVisitsDetailsById(visitDetailsId: String): VisitEntity = visitDao.getVisitsDetailsById(visitId = visitDetailsId)

    suspend fun updateVisitCheckInTimesAndSync(
        visitDetailsId: String,
        actualStartTime: String,
        actualStartTimeString: String,
        visitStatus: String,
        checkInSync: Boolean
    ) {
        visitDao.updateVisitCheckInTimesAndSync(
            visitDetailsId = visitDetailsId,
            actualStartTime = actualStartTime,
            actualStartTimeString = actualStartTimeString,
            visitStatus = visitStatus,
            checkInSync = checkInSync
        )
    }

    suspend fun updateVisitCheckOutTimesAndSync(
        visitDetailsId: String,
        actualEndTime: String,
        actualEndTimeString: String,
        totalActualTimeDiff: String,
        visitStatus: String,
        checkOutSync: Boolean
    ) {
        visitDao.updateVisitCheckOutTimesAndSync(
            visitDetailsId = visitDetailsId,
            actualEndTime = actualEndTime,
            actualEndTimeString = actualEndTimeString,
            totalActualTimeDiff = totalActualTimeDiff,
            visitStatus = visitStatus,
            checkOutSync = checkOutSync
        )
    }

    suspend fun getUatId(visitDetailsId: String): String = visitDao.getUatId(visitId = visitDetailsId)

    suspend fun validateQrCode(clientId: String, qrcodeToken: String): Boolean = visitDao.validateQrCode(clientId, qrcodeToken)

    suspend fun getActualStartTimeString(visitDetailsId: String): String = visitDao.getActualStartTimeString(visitId = visitDetailsId)

    suspend fun insertAutoAlerts(autoAlertEntity: AutoAlertEntity) = visitDao.insertAutoAlerts(autoAlertEntity)

    suspend fun getTodoListByVisitsDetailsId(visitDetailsId: String): List<TodoEntity> = visitDao.getTodoListByVisitsDetailsId(visitId = visitDetailsId)

    suspend fun updateTodoListById(
        todoDetailsId: String,
        carerNotes: String,
        todoOutcome: String,
        todoSync: Boolean
    ) {
        visitDao.updateTodoListById(
            todoDetailsId = todoDetailsId,
            carerNotes = carerNotes,
            todoOutcome = todoOutcome,
            todoSync = todoSync
        )
    }

    suspend fun getMedicationListByVisitsDetailsId(visitDetailsId: String): List<MedicationEntity> = visitDao.getMedicationListByVisitsDetailsId(visitId = visitDetailsId)

    suspend fun updateMedicationByBlisterPackDetailsId(
        blisterPackDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationBlisterPack: Boolean
    ) {
        visitDao.updateMedicationByBlisterPackDetailsId(
            blisterPackDetailsId = blisterPackDetailsId,
            status = status,
            carerNotes = carerNotes,
            medicationSync = medicationSync,
            medicationBlisterPack = medicationBlisterPack
        )
    }

    suspend fun updateMedicationByScheduledDetailsId(
        scheduledDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationScheduled: Boolean
    ) {
        visitDao.updateMedicationByScheduledDetailsId(
            scheduledDetailsId = scheduledDetailsId,
            status = status,
            carerNotes = carerNotes,
            medicationSync = medicationSync,
            medicationScheduled = medicationScheduled
        )
    }

    suspend fun updateMedicationByPrnDetailsId(
        prnDetailsId: String,
        status: String,
        carerNotes: String,
        medicationSync: Boolean,
        medicationPrnUpdate: Boolean
    ) {
        visitDao.updateMedicationByPrnDetailsId(
            prnDetailsId = prnDetailsId,
            status = status,
            carerNotes = carerNotes,
            medicationSync = medicationSync,
            medicationPrnUpdate = medicationPrnUpdate
        )
    }

    suspend fun insertVisitNotes(visitNotes: VisitNotesEntity) = visitDao.insertVisitNotes(visitNotes)

    suspend fun getAllVisitNotesByVisitDetailsId(visitDetailsId: String): List<VisitNotesEntity> = visitDao.getAllVisitNotesByVisitDetailsId(visitDetailsId)

    suspend fun updateVisitNotesById(
        visitNotesId: String,
        visitNotes: String,
        updatedByUserid: String,
        updatedByUserName: String,
        updatedAt: String
    ) {
        visitDao.updateVisitNotesById(
            visitNotesId = visitNotesId,
            visitNotes = visitNotes,
            updatedByUserid = updatedByUserid,
            updatedByUserName = updatedByUserName,
            updatedAt = updatedAt
        )
    }

    fun getTodosWithEssentialAndEmptyOutcome(visitDetailsId: String): List<TodoEntity> = visitDao.getTodosWithEssentialAndEmptyOutcome(visitDetailsId)
    fun getMedicationsWithScheduled(visitDetailsId: String): List<MedicationEntity> = visitDao.getMedicationsWithScheduled(visitDetailsId)

    // SYNC CHECK-IN
    suspend fun getVisitsForCheckInSync(): List<VisitEntity> = visitDao.getVisitsForCheckInSync()
    suspend fun updateVisitCheckInFinish(visitDetailsId: String) = visitDao.updateVisitCheckInFinish(visitDetailsId)

    // SYNC CHECK-IN AUTO ALERTS
    suspend fun getVisitsForCheckInAlertSync(alertAction: Int): List<AutoAlertEntity> = visitDao.getVisitsForCheckInAlertSync(alertAction)
    suspend fun updateVisitCheckInAlertFinish(columnId: String) = visitDao.updateVisitCheckInAlertFinish(columnId)

    // SYNC TODO
    suspend fun getTodoSync(): List<TodoEntity> = visitDao.getTodoSync()
    suspend fun updateTodoFinish(todoDetailsId: String) = visitDao.updateTodoFinish(todoDetailsId)

    // SYNC MEDICATION
    suspend fun getMedicationSync(): List<MedicationEntity> = visitDao.getMedicationSync()
    suspend fun updateMedicationFinish(createdAt: Long) = visitDao.updateMedicationFinish(createdAt)

    // SYNC MEDICATION
    suspend fun getVisitNotesSync(): List<VisitNotesEntity> = visitDao.getVisitNotesSync()
    suspend fun updateVisitNotesFinish(visitNotesId: String) = visitDao.updateVisitNotesFinish(visitNotesId)

    // SYNC CHECKOUT
    suspend fun getVisitsForCheckOutSync(): List<VisitEntity> = visitDao.getVisitsForCheckOutSync()
    suspend fun updateVisitCheckOutFinish(visitDetailsId: String) = visitDao.updateVisitCheckOutFinish(visitDetailsId)

}
