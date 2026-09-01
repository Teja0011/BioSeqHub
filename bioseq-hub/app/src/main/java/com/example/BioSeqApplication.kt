package com.example

import android.app.Application
import com.example.data.local.database.BioSeqDatabase
import com.example.data.remote.BioSeqRemoteDataSource
import com.example.data.repository.BioSeqRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BioSeqApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { BioSeqDatabase.getDatabase(this, applicationScope) }
    val remoteDataSource by lazy { BioSeqRemoteDataSource() }
    val repository by lazy { BioSeqRepository(database.bioSeqDao(), remoteDataSource) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: BioSeqApplication
            private set
    }
}
