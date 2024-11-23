package com.param.newsbit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.param.newsbit.entity.NewsFilter
import com.param.newsbit.model.parser.NewsGenre
import com.param.newsbit.notifaction.NewsNotificationService
import com.param.newsbit.repo.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class NewsDownloadWorker @AssistedInject constructor(
    @Assisted private val repository: Repository,
    @Assisted private val newsNotificationService: NewsNotificationService,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val TAG = javaClass.simpleName

    override suspend fun doWork(): Result {

        Log.i(TAG, "doWork: stared")

        return try {

            val defaultFilter = NewsFilter("", "", LocalDate.now(), LocalDate.now())

            val before = repository.countBy(defaultFilter)

            NewsGenre.TITLES.forEach { repository.downloadNews(it,20) }

            val after = repository.countBy(defaultFilter)

            Log.i(TAG, "doWork: before:$before after:$after")

            val newCount = after - before

            if (newCount > 0) {
                newsNotificationService.showNotification(newCount)
            }

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "doWork: ${e.message}")
            Result.failure()
        }

    }

}