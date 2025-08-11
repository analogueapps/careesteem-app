package com.aits.careesteem.room.repo

import com.aits.careesteem.room.dao.VisitDao
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import javax.inject.Inject

class VisitRepository @Inject constructor(
    private val visitDao: VisitDao
) {

    suspend fun insertVisit(visit: VisitEntity) = visitDao.insertVisit(visit)

    suspend fun insertMedications(meds: List<MedicationEntity>) = visitDao.insertMedications(meds)

    suspend fun insertTodos(todos: List<TodoEntity>) = visitDao.insertTodos(todos)

    suspend fun getAllVisits(): List<VisitEntity> = visitDao.getAllVisits()

    suspend fun getAllVisitsByDate(visitDate: String): List<VisitEntity> = visitDao.getAllVisitsByDate(visitDate = visitDate)

    suspend fun getVisitsDetailsById(visitDetailsId: String): VisitEntity = visitDao.getVisitsDetailsById(visitId = visitDetailsId)

    suspend fun updateVisitCheckInTimesAndSync(
        visitDetailsId: String,
        actualStartTime: String,
        actualStartTimeString: String,
        checkInSync: Boolean
    ) {
        visitDao.updateVisitCheckInTimesAndSync(
            visitDetailsId = visitDetailsId,
            actualStartTime = actualStartTime,
            actualStartTimeString = actualStartTimeString,
            checkInSync = checkInSync
        )
    }

    suspend fun updateVisitCheckOutTimesAndSync(
        visitDetailsId: String,
        actualEndTime: String,
        actualEndTimeString: String,
        checkOutSync: Boolean
    ) {
        visitDao.updateVisitCheckOutTimesAndSync(
            visitDetailsId = visitDetailsId,
            actualEndTime = actualEndTime,
            actualEndTimeString = actualEndTimeString,
            checkOutSync = checkOutSync
        )
    }

    suspend fun getUatId(visitDetailsId: String): String = visitDao.getUatId(visitId = visitDetailsId)

    suspend fun insertAutoAlerts(autoAlertEntity: AutoAlertEntity) = visitDao.insertAutoAlerts(autoAlertEntity)

    suspend fun getTodoListByVisitsDetailsId(visitDetailsId: String): List<TodoEntity> = visitDao.getTodoListByVisitsDetailsId(visitId = visitDetailsId)

    suspend fun updateTodoListById(
        todoDetailsId: String,
        carerNotes: String,
        todoOutcome: Boolean,
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


}
