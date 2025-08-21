package com.aits.careesteem.service.manager

import com.aits.careesteem.service.repo.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncRepo: SyncRepository
) {
    fun startSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncRepo.syncAll()
        }
    }
}