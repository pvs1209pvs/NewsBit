package com.param.newsbit.di

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.param.newsbit.AbstractApplication
import com.param.newsbit.repo.Repository
import com.param.newsbit.worker.NewsDownloadWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class MyApp : AbstractApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()


}

class CustomWorkerFactory @Inject constructor(private val repository: Repository) :
    WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return NewsDownloadWorker(repository, appContext, workerParameters)
    }

}