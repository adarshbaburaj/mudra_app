package com.ada.mudra

import android.app.Application
import com.ada.mudra.data.MudraRepository
import com.ada.mudra.data.remote.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MudraApp : Application() {

    val repository: MudraRepository by lazy { MudraRepository(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val syncManager: SyncManager by lazy { SyncManager(repository, appScope) }

    override fun onCreate() {
        super.onCreate()
        // Touch lazily so session restore + realtime start with the app.
        syncManager
    }
}
