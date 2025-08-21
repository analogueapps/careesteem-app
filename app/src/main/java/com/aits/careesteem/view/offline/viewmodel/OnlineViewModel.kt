package com.aits.careesteem.view.offline.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aits.careesteem.network.Repository
import com.aits.careesteem.room.repo.VisitRepository
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.visits.db_entity.AutoAlertEntity
import com.aits.careesteem.view.visits.db_entity.MedicationEntity
import com.aits.careesteem.view.visits.db_entity.TodoEntity
import com.aits.careesteem.view.visits.db_entity.VisitEntity
import com.aits.careesteem.view.visits.db_entity.VisitNotesEntity
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnlineViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val dbRepository: VisitRepository
): ViewModel() {

    val gson = Gson()
    val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
    val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

    // ✅ Progress tracking
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> = _isCompleted

    private var totalTasks = 0
    private var completedTasks = 0

    /**
     * Main sync method
     */
    suspend fun syncAll() {
        _progress.postValue(0)
        _isCompleted.postValue(false)
        completedTasks = 0
        totalTasks = calculateTotalTasks()

        // Step 1: Sync CheckIn and related alerts
        syncCheckIn()
        syncCheckInAlerts()

        // Step 2: Sync Todos and related alerts
        syncTodos()
        syncTodoAlerts()

        // Step 3: Sync Medication and related alerts
        syncMedication()
        syncMedicationAlerts()

        // Step 4: Sync Visit Notes
        syncVisitNotes()

        // Step 5: Sync Checkout and related alerts
        syncCheckOut()
        syncCheckOutAlerts()

        // ✅ If everything finished
        _isCompleted.postValue(true)
    }

    /**
     * Calculate how many tasks need to be synced
     */
    private suspend fun calculateTotalTasks(): Int {
        return dbRepository.getVisitsForCheckInSync().size +
                dbRepository.getVisitsForCheckInAlertSync(1).size +
                dbRepository.getTodoSync().size +
                dbRepository.getVisitsForCheckInAlertSync(2).size +
                dbRepository.getMedicationSync().size +
                dbRepository.getVisitsForCheckInAlertSync(3).size +
                dbRepository.getVisitNotesSync().size +
                dbRepository.getVisitsForCheckOutSync().size
    }

    /**
     * Helper function to update progress
     */
    private fun incrementProgress() {
        completedTasks++
        val percent = if (totalTasks == 0) 100 else (completedTasks * 100) / totalTasks
        _progress.postValue(percent)
    }

    suspend fun syncCheckIn() {
        val entityList = dbRepository.getVisitsForCheckInSync()
        entityList.forEach {
            syncCheckInApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncCheckInApi(visitEntity: VisitEntity) {
        try {
            val response = repository.addVisitCheckInOffline(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                clientId = visitEntity.clientId!!,
                visitDetailsId = visitEntity.visitDetailsId,
                id = visitEntity.uatId!!,
                userId = userData.id,
                status = "checkin",
                actualStartTime = visitEntity.actualStartTimeString!!,
                createdAt = DateTimeUtils.getCurrentTimestampForCheckOutGMT()
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitCheckInFinish(visitEntity.visitDetailsId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncCheckInAlerts() {
        val entityList = dbRepository.getVisitsForCheckInAlertSync(1)
        entityList.forEach {
            if(it.alertType == "Force Check-In" || it.alertType == "Early Check-In") {
                syncCheckInAlertsApi(it)
                incrementProgress()
            }
        }
    }

    private suspend fun syncCheckInAlertsApi(entity: AutoAlertEntity) {
        try {
            val response = repository.automaticAlerts(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                uatId = entity.uatId!!,
                visitDetailsId = entity.visitDetailsId!!,
                clientId = entity.clientId!!,
                alertType = entity.alertType!!,
                alertStatus = "Action Required",
                createdAt = entity.createdAt!!,
                userId = userData.id,
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitCheckInAlertFinish(entity.colId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncTodos() {
        val entityList = dbRepository.getTodoSync()
        entityList.forEach {
            syncTodosApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncTodosApi(entity: TodoEntity) {
        try {
            val hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null).orEmpty()
            val todoOutcomeValue = if (entity.todoOutcome == "Completed") 1 else 0

            val response = repository.updateTodoDetails(
                hashToken = hashToken,
                todoId = entity.todoDetailsId,
                carerNotes = entity.carerNotes!!,
                todoOutcome = todoOutcomeValue
            )
            if(response.isSuccessful) {
                dbRepository.updateTodoFinish(entity.todoDetailsId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncTodoAlerts() {
        val entityList = dbRepository.getVisitsForCheckInAlertSync(2)
        entityList.forEach {
            syncTodoAlertsApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncTodoAlertsApi(entity: AutoAlertEntity) {
        try {
            val response = repository.automaticTodoAlerts(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                todoDetailsId = entity.todoDetailsId!!,
                visitDetailsId = entity.visitDetailsId!!,
                clientId = entity.clientId!!,
                alertType = entity.alertType!!,
                alertStatus = entity.alertStatus!!,
                createdAt = entity.createdAt!!,
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitCheckInAlertFinish(entity.colId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncMedication() {
        val entityList = dbRepository.getMedicationSync()
        entityList.forEach {
            if(it.medicationPrn!!) {
                syncMedicationPrnAdd(it)
                incrementProgress()
            } else if(it.medicationBlisterPack!!) {
                syncMedicationBlisterPack(it)
                incrementProgress()
            } else if(it.medicationScheduled!!) {
                syncMedicationScheduled(it)
                incrementProgress()
            }  else if(it.medicationPrnUpdate!!) {
                syncMedicationPrnUpdate(it)
                incrementProgress()
            }
        }
    }

    private suspend fun syncMedicationPrnAdd(entity: MedicationEntity) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(entity.createdAt))

            val response = repository.medicationPrnDetailsOffline(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                id = entity.prn_details_id!!,
                clientId = entity.client_id!!,
                medicationId = entity.medication_id,
                prnId = entity.prn_id!!,
                doesPer = entity.dose_per!!,
                doses = entity.doses!!,
                timeFrame = entity.time_frame!!,
                prnOffered = entity.prn_offered!!,
                prnBeGiven = entity.prn_be_given!!,
                visitDetailsId = entity.visitDetailsId,
                userId = userData.id,
                medicationTime = "",
                createdAt = formattedDate,
                carerNotes = entity.carer_notes!!,
                status = entity.status!!,
            )
            if(response.isSuccessful) {
                dbRepository.updateMedicationFinish(entity.createdAt)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncMedicationBlisterPack(entity: MedicationEntity) {
        try {
            val response = repository.medicationBpDetails(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                blisterPackDetailsId = entity.blister_pack_details_id!!,
                status = entity.status!!,
                carerNotes = entity.carer_notes!!
            )
            if(response.isSuccessful) {
                dbRepository.updateMedicationFinish(entity.createdAt)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncMedicationScheduled(entity: MedicationEntity) {
        try {
            val response = repository.medicationScheduledDetails(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                scheduledDetailsId = entity.scheduled_details_id!!,
                status = entity.status!!,
                carerNotes = entity.carer_notes!!
            )
            if(response.isSuccessful) {
                dbRepository.updateMedicationFinish(entity.createdAt)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncMedicationPrnUpdate(entity: MedicationEntity) {
        try {
            val response = repository.updateMedicationPrn(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                prnDetailsId = entity.prn_details_id!!,
                status = entity.status!!,
                carerNotes = entity.carer_notes!!
            )
            if(response.isSuccessful) {
                dbRepository.updateMedicationFinish(entity.createdAt)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncMedicationAlerts() {
        val entityList = dbRepository.getVisitsForCheckInAlertSync(3)
        entityList.forEach {
            syncMedicationAlertsApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncMedicationAlertsApi(entity: AutoAlertEntity) {
        try {
            val response = repository.automaticMedicationAlerts(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                scheduledId = entity.scheduledId!!,
                blisterPackId = entity.blisterPackId!!,
                visitDetailsId = entity.visitDetailsId!!,
                clientId = entity.clientId!!,
                alertType = entity.alertType!!,
                alertStatus = entity.alertStatus!!,
                createdAt = entity.createdAt!!
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitCheckInAlertFinish(entity.colId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncVisitNotes() {
        val entityList = dbRepository.getVisitNotesSync()
        entityList.forEach {
            syncVisitNotesApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncVisitNotesApi(entity: VisitNotesEntity) {
        try {
            val response = repository.addClientVisitNotesDetails(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                visitDetailsId = entity.visitDetailsId!!,
                visitNotes = entity.visitNotes!!,
                createdByUserid = entity.createdByUserid!!,
                updatedByUserid = entity.updatedByUserid!!,
                createdAt = entity.createdAt!!
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitNotesFinish(entity.visitNotesId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncCheckOut() {
        val entityList = dbRepository.getVisitsForCheckOutSync()
        entityList.forEach {
            syncCheckOutApi(it)
            incrementProgress()
        }
    }

    private suspend fun syncCheckOutApi(visitEntity: VisitEntity) {
        try {
            val response = repository.updateVisitCheckout(
                hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                    .toString(),
                userId = userData.id,
                visitDetailsId = visitEntity.visitDetailsId,
                actualEndTime = visitEntity.actualEndTimeString!!,
                status = "checkout",
                updatedAt = visitEntity.actualEndTimeString!!
            )
            if(response.isSuccessful) {
                dbRepository.updateVisitCheckInFinish(visitEntity.visitDetailsId)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncCheckOutAlerts() {
        val entityList = dbRepository.getVisitsForCheckInAlertSync(1)
        entityList.forEach {
            if(it.alertType == "Force Check-Out" || it.alertType == "Early Check-Out") {
                syncCheckInAlertsApi(it)
                incrementProgress()
            }
        }
    }
}