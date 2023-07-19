package com.param.newsbit

import androidx.compose.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.param.newsbit.model.database.LocalDatabase
import com.param.newsbit.model.parser.FeedURL
import com.param.newsbit.model.repo.Repository
import java.time.LocalDate

class FeedRefreshedWorker(
    private val context: Context,
    private val workParams: WorkerParameters
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {

//        val newsDao = LocalDatabase.getDatabase(context).newsDao()
//        val repo = Repository(newsDao)
//
//        val nowDate = LocalDate.now()


        FeedURL.genre.keys.forEach {
            println(it)
//            repo.fetchFromCloud(it, nowDate)
        }

        return Result.success()

    }

}