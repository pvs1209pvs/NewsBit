package com.param.newsbit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.paging.LOG_TAG
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.param.newsbit.model.parser.NewsGenre
import com.param.newsbit.repo.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NewsDownloadWorker @AssistedInject constructor(
    @Assisted private val repository: Repository,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val TAG = javaClass.simpleName

    override suspend fun doWork(): Result {

        Log.i(TAG, "doWork: Downloading all genres")

        try {
            NewsGenre.TITLES.forEach { repository.downloadNews(it) }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }

        return Result.failure()

    }


}