package com.param.newsbit.worker

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
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

            val empty = NewsFilter.empty()

            val before = repository.countBy(empty)

            NewsGenre.TITLES.forEach { repository.downloadNews(it, 50) }

            val after = repository.countBy(empty)

            Log.i(TAG, "doWork: before:$before after:$after")

            val newCount = after-before

            if (newCount > 0) {
                newsNotificationService.showNotification(newCount.toString())
            }


            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "doWork: ${e.message}")
            Result.failure()
        }

    }

    fun isAppForground(context: Context): Boolean {

        val mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (info in mActivityManager.runningAppProcesses) {
            if (info.uid == context.applicationInfo.uid && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false

    }

}