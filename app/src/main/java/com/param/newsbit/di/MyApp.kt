package com.param.newsbit.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.param.newsbit.notifaction.NewsNotificationService
import com.param.newsbit.repo.Repository
import com.param.newsbit.worker.NewsDownloadWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class MyApp : Application(), Configuration.Provider {

    private val TAG = javaClass.simpleName

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()


}

class CustomWorkerFactory @Inject constructor(
    private val repository: Repository,
    private val newsNotificationService: NewsNotificationService,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return NewsDownloadWorker(repository, newsNotificationService, appContext, workerParameters)
    }

}